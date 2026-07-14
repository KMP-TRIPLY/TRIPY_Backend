package com.kmp.Triply.domain.ranking.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.ranking.dto.request.RankingMode;
import com.kmp.Triply.domain.ranking.dto.response.RankingEntryResponse;
import com.kmp.Triply.domain.ranking.dto.response.RankingResponse;
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
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final MissionAttemptRepository missionAttemptRepository;

    @Override
    public RankingResponse getLiveRankings(Long gameRoomId, RankingMode mode) {
        GameRoom gameRoom = gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));
        if (gameRoom.getStatus() != GameStatus.RUNNING) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }

        List<RankingEntryResponse> rankings = switch (mode) {
            case TEAM -> getLiveTeamRankings(gameRoomId);
            case PERSONAL -> toPersonalRankings(missionAttemptRepository.findPersonalLiveRankingsByGameRoomId(gameRoomId));
        };

        return RankingResponse.of(
                mode,
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
            case TEAM -> getCourseTeamRankings(courseId);
            case PERSONAL -> toPersonalRankings(missionAttemptRepository.findPersonalCourseRankingsByCourseId(courseId));
        };

        return RankingResponse.of(mode, null, course.getId(), course.getTitle(), rankings);
    }

    private List<RankingEntryResponse> getLiveTeamRankings(Long gameRoomId) {
        List<Team> teams = teamRepository.findAllByGameRoomIdOrderByTotalScoreDescCreatedAtAsc(gameRoomId);
        return IntStream.range(0, teams.size())
                .mapToObj(index -> RankingEntryResponse.of(
                        index + 1,
                        teams.get(index).getId(),
                        teams.get(index).getTeamName(),
                        teams.get(index).getTotalScore(),
                        null,
                        null,
                        teams.get(index).getHintCountUsed()
                ))
                .toList();
    }

    private List<RankingEntryResponse> getCourseTeamRankings(Long courseId) {
        List<Team> teams = teamRepository.findAllByGameRoomCourseIdAndGameRoomStatusOrderByTotalScoreDescCreatedAtAsc(
                courseId,
                GameStatus.FINISHED
        );
        return IntStream.range(0, teams.size())
                .mapToObj(index -> RankingEntryResponse.of(
                        index + 1,
                        teams.get(index).getId(),
                        teams.get(index).getTeamName(),
                        teams.get(index).getTotalScore(),
                        null,
                        null,
                        teams.get(index).getHintCountUsed()
                ))
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
