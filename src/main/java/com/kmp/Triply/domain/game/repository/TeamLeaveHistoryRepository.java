package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.TeamLeaveHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamLeaveHistoryRepository extends JpaRepository<TeamLeaveHistory, Long> {

    @Query("""
            select coalesce(sum(history.preservedScore), 0)
            from TeamLeaveHistory history
            where history.team.id = :teamId
            """)
    int sumPreservedScoreByTeamId(@Param("teamId") Long teamId);
}
