package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionAttemptRepository extends JpaRepository<MissionAttempt, Long> {

    @Query("""
            select coalesce(sum(missionAttempt.scoreEarned), 0)
            from MissionAttempt missionAttempt
            where missionAttempt.gameProgress.team.id = :teamId
            """)
    int sumScoreByTeamId(@Param("teamId") Long teamId);

    @Query("""
            select coalesce(sum(missionAttempt.scoreEarned), 0)
            from MissionAttempt missionAttempt
            where missionAttempt.gameProgress.team.id = :teamId
              and missionAttempt.user.id = :userId
            """)
    int sumScoreByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    @Query("""
            select missionAttempt.user.id, missionAttempt.user.nickname, coalesce(sum(missionAttempt.scoreEarned), 0)
            from MissionAttempt missionAttempt
            where missionAttempt.gameProgress.team.gameRoom.id = :gameRoomId
            group by missionAttempt.user.id, missionAttempt.user.nickname
            order by coalesce(sum(missionAttempt.scoreEarned), 0) desc, missionAttempt.user.id asc
            """)
    List<Object[]> findPersonalLiveRankingsByGameRoomId(Long gameRoomId);

    @Query("""
            select missionAttempt.user.id,
                   missionAttempt.user.nickname,
                   coalesce(sum(missionAttempt.scoreEarned), 0),
                   coalesce(sum(case when missionAttempt.result = com.kmp.Triply.domain.game.entity.AttemptResult.CORRECT then 1 else 0 end), 0),
                   coalesce(sum(case when missionAttempt.hintUsed = true then 1 else 0 end), 0)
            from MissionAttempt missionAttempt
            where missionAttempt.gameProgress.team.gameRoom.id = :gameRoomId
            group by missionAttempt.user.id, missionAttempt.user.nickname
            order by coalesce(sum(missionAttempt.scoreEarned), 0) desc, missionAttempt.user.id asc
            """)
    List<Object[]> findPersonalFinalRankingRowsByGameRoomId(Long gameRoomId);

    @Query("""
            select missionAttempt.user.id, missionAttempt.user.nickname, coalesce(sum(missionAttempt.scoreEarned), 0)
            from MissionAttempt missionAttempt
            where missionAttempt.gameProgress.team.gameRoom.course.id = :courseId
              and missionAttempt.gameProgress.team.gameRoom.status = com.kmp.Triply.domain.game.entity.GameStatus.FINISHED
            group by missionAttempt.user.id, missionAttempt.user.nickname
            order by coalesce(sum(missionAttempt.scoreEarned), 0) desc, missionAttempt.user.id asc
            """)
    List<Object[]> findPersonalCourseRankingsByCourseId(Long courseId);
}
