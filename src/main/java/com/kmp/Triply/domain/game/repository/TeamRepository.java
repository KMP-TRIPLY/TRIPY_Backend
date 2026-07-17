package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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

    long countByGameRoomId(Long gameRoomId);
}
