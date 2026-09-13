package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.reward.RewardCatalog;
import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;
import com.kmp.Triply.domain.reward.entity.Reward;
import com.kmp.Triply.domain.reward.entity.UserReward;
import com.kmp.Triply.domain.reward.repository.RewardRepository;
import com.kmp.Triply.domain.reward.repository.UserRewardRepository;
import com.kmp.Triply.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게임 종료 시점의 결과를 보고 리워드를 적립한다.
 *
 * <p>기준 데이터({@code rewards})가 비어 있으면 적립할 대상이 없다는 뜻이므로
 * 예외를 던지지 않고 건너뛴다. 여기서 터지면 게임 종료 자체가 롤백되는데,
 * 보상 하나 못 준 것 때문에 확정된 순위를 잃는 편이 훨씬 나쁘다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameRewardGrantServiceImpl implements GameRewardGrantService {

    /** "미션 해결사" 기준. 한 게임에서 이 개수 이상 정답 처리하면 준다. */
    private static final int MISSION_SOLVER_THRESHOLD = 2;

    private static final short WINNER_RANK = 1;

    private final RewardRepository rewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MissionAttemptRepository missionAttemptRepository;

    @Override
    @Transactional
    public List<UserRewardResponse> grantForFinishedGame(GameRoom gameRoom) {
        // 완주 보상은 끝까지 남아 있던 팀원 기준이다. 하차한 멤버는 완주한 것이 아니므로 제외된다.
        List<TeamMember> members = teamMemberRepository
                .findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(gameRoom.getId());
        if (members.isEmpty()) {
            return List.of();
        }

        Map<Long, PersonalStat> statsByUserId = personalStatsByUserId(gameRoom);
        List<UserRewardResponse> granted = new ArrayList<>();

        for (TeamMember member : members) {
            User user = member.getUser();

            grant(RewardCatalog.FIRST_JOURNEY, user, gameRoom).ifPresent(granted::add);

            Short teamRank = member.getTeam().getRank();
            if (teamRank != null && teamRank == WINNER_RANK) {
                grant(RewardCatalog.FIRST_WIN, user, gameRoom).ifPresent(granted::add);
            }

            // 미션을 한 번도 시도하지 않은 멤버는 집계 행이 없다. 완주 보상만 받고 끝난다.
            PersonalStat stat = statsByUserId.get(user.getId());
            if (stat == null) {
                continue;
            }
            if (stat.correctCount() > 0 && stat.hintUsedCount() == 0) {
                grant(RewardCatalog.NO_HINT_CLEAR, user, gameRoom).ifPresent(granted::add);
            }
            if (stat.correctCount() >= MISSION_SOLVER_THRESHOLD) {
                grant(RewardCatalog.MISSION_SOLVER, user, gameRoom).ifPresent(granted::add);
            }
        }

        if (!granted.isEmpty()) {
            log.info("게임방 {} 종료 리워드 {}건을 적립했습니다.", gameRoom.getId(), granted.size());
        }
        return granted;
    }

    /**
     * 게임방의 유저별 정답 수·힌트 사용 수. 최종 개인 랭킹 집계 쿼리를 그대로 쓴다
     * (랭킹과 보상이 같은 숫자를 보게 하려는 의도다).
     */
    private Map<Long, PersonalStat> personalStatsByUserId(GameRoom gameRoom) {
        Map<Long, PersonalStat> stats = new HashMap<>();
        for (Object[] row : missionAttemptRepository.findPersonalFinalRankingRowsByGameRoomId(gameRoom.getId())) {
            stats.put((Long) row[0], new PersonalStat(
                    ((Number) row[3]).intValue(),
                    ((Number) row[4]).intValue()));
        }
        return stats;
    }

    private Optional<UserRewardResponse> grant(RewardCatalog definition, User user, GameRoom gameRoom) {
        Optional<Reward> reward = rewardRepository.findByName(definition.getRewardName());
        if (reward.isEmpty()) {
            log.warn("리워드 마스터 '{}' 가 없어 적립을 건너뜁니다. rewards 시드가 돌았는지 확인하세요.",
                    definition.getRewardName());
            return Optional.empty();
        }

        // user_rewards 는 (user_id, reward_id) 유니크다. 이미 가진 업적이면 조용히 넘긴다.
        if (userRewardRepository.existsByUserIdAndRewardId(user.getId(), reward.get().getId())) {
            return Optional.empty();
        }

        UserReward saved = userRewardRepository.save(UserReward.builder()
                .user(user)
                .reward(reward.get())
                .gameRoom(gameRoom)
                .build());
        return Optional.of(UserRewardResponse.from(saved));
    }

    private record PersonalStat(int correctCount, int hintUsedCount) {
    }
}
