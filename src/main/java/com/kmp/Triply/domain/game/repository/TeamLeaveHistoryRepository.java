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

    /** 스스로 하차한 적이 있는지. 있으면 그 방에는 다시 들어올 수 없다. */
    boolean existsByGameRoomIdAndUserId(Long gameRoomId, Long userId);
}
