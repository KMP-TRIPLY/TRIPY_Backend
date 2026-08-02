package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.AttemptResult;
import com.kmp.Triply.domain.game.entity.AttemptType;
import com.kmp.Triply.domain.game.entity.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionAttemptRepository extends JpaRepository<MissionAttempt, Long> {

    boolean existsByGameProgressIdAndMissionIdAndResult(Long gameProgressId, Long missionId, AttemptResult result);

    boolean existsByGameProgressIdAndMissionIdAndAttemptType(Long gameProgressId, Long missionId, AttemptType attemptType);

    long countByGameProgressIdAndResult(Long gameProgressId, AttemptResult result);

    List<MissionAttempt> findAllByGameProgressIdAndResult(Long gameProgressId, AttemptResult result);

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
