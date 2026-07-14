package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
