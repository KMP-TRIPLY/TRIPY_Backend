package com.kmp.Triply.domain.ranking.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.ranking.dto.request.RankingMode;
import com.kmp.Triply.domain.ranking.dto.response.RankingEntryResponse;
import com.kmp.Triply.domain.ranking.dto.response.RankingResponse;
import com.kmp.Triply.domain.ranking.entity.Ranking;
import com.kmp.Triply.domain.ranking.entity.RankingType;
import com.kmp.Triply.domain.ranking.repository.RankingRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final GameRoomRepository gameRoomRepository;
    private final CourseRepository courseRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final RankingRepository rankingRepository;

    @Override
    public RankingResponse getLiveRankings(Long gameRoomId) {
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));
        if (gameRoom.getStatus() != GameStatus.RUNNING) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }

        // 방 하나에 팀 하나이므로 방 안에서 겨룰 상대는 멤버끼리뿐이다. 방 vs 방은 코스 랭킹에서 본다.
        List<RankingEntryResponse> rankings = toPersonalRankings(
                missionAttemptRepository.findPersonalLiveRankingsByGameRoomId(gameRoomId));

        return RankingResponse.of(
                RankingMode.PERSONAL,
                gameRoom.getId(),
                gameRoom.getCourse().getId(),
                gameRoom.getCourse().getTitle(),
                rankings
        );
    }

    @Override
    public RankingResponse getCourseRankings(Long courseId, RankingMode mode) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        List<RankingEntryResponse> rankings = switch (mode) {
            case ROOM -> getCourseRoomRankings(courseId);
            case PERSONAL -> getCoursePersonalRankings(courseId);
        };

        return RankingResponse.of(mode, null, course.getId(), course.getTitle(), rankings);
    }

    private List<RankingEntryResponse> getCourseRoomRankings(Long courseId) {
        List<Ranking> storedRankings = rankingRepository.findAllByGameRoomCourseIdAndRankingTypeOrderByFinalScoreDescRankAsc(
                courseId,
                RankingType.ROOM
        );
        return toStoredRankings(storedRankings);
    }

    private List<RankingEntryResponse> getCoursePersonalRankings(Long courseId) {
        List<Ranking> storedRankings = rankingRepository.findAllByGameRoomCourseIdAndRankingTypeOrderByFinalScoreDescRankAsc(
                courseId,
                RankingType.PERSONAL
        );
        return toStoredRankings(storedRankings);
    }

    private List<RankingEntryResponse> toStoredRankings(List<Ranking> rankings) {
        return IntStream.range(0, rankings.size())
                .mapToObj(index -> {
                    Ranking ranking = rankings.get(index);
                    Long targetId = ranking.getRankingType() == RankingType.ROOM
                            ? ranking.getTeam().getGameRoom().getId()
                            : ranking.getUser().getId();
                    String targetName = ranking.getRankingType() == RankingType.ROOM
                            ? ranking.getTeam().getTeamName()
                            : ranking.getUser().getNickname();
                    return RankingEntryResponse.of(
                            index + 1,
                            targetId,
                            targetName,
                            ranking.getFinalScore(),
                            ranking.getElapsedSeconds(),
                            ranking.getMissionClearCount(),
                            ranking.getHintUsedCount()
                    );
                })
                .toList();
    }

    private List<RankingEntryResponse> toPersonalRankings(List<Object[]> rows) {
        return IntStream.range(0, rows.size())
                .mapToObj(index -> RankingEntryResponse.of(
                        index + 1,
                        (Long) rows.get(index)[0],
                        (String) rows.get(index)[1],
                        ((Number) rows.get(index)[2]).intValue(),
                        null,
                        null,
                        null
                ))
                .toList();
    }
}
