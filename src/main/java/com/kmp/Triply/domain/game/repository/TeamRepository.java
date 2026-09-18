package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByGameRoomId(Long gameRoomId);

    List<Team> findAllByGameRoomIdOrderByTotalScoreDescCreatedAtAsc(Long gameRoomId);

    List<Team> findAllByGameRoomCourseIdAndGameRoomStatusOrderByTotalScoreDescCreatedAtAsc(
            Long courseId,
            com.kmp.Triply.domain.game.entity.GameStatus gameStatus
    );

    @Query("""
            select team,
                   coalesce(sum(missionAttempt.scoreEarned), 0),
                   coalesce(sum(case when missionAttempt.result = com.kmp.Triply.domain.game.entity.AttemptResult.CORRECT then 1 else 0 end), 0),
                   coalesce(sum(case when missionAttempt.hintUsed = true then 1 else 0 end), 0)
            from Team team
            left join GameProgress gameProgress on gameProgress.team = team
            left join MissionAttempt missionAttempt on missionAttempt.gameProgress = gameProgress
            where team.gameRoom.id = :gameRoomId
            group by team
            order by coalesce(sum(missionAttempt.scoreEarned), 0) desc, team.createdAt asc
            """)
    List<Object[]> findTeamRankingRowsByGameRoomId(Long gameRoomId);

    Optional<Team> findFirstByGameRoomIdOrderByIdAsc(Long gameRoomId);

    /**
     * 방 하나에 팀 하나. 방을 만들 때 같이 만들어지므로 항상 존재한다.
     * ponytail: teams 테이블을 GameRoom 에 흡수하지 않고 1:1 로 남겨둔 상태 —
     * 점수·진행상황 컬럼과 reward·ranking 도메인의 FK 를 옮기는 마이그레이션이 필요해 별건으로 미뤘다.
     */
    default Team findOfRoom(Long gameRoomId) {
        return findFirstByGameRoomIdOrderByIdAsc(gameRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }
}
