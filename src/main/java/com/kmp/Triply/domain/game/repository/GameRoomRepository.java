package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    boolean existsByRoomCode(String roomCode);

    /**
     * 방 목록 한 줄에 필요한 것: 방 자체 + 방 이름(팀 이름) + 지금 인원.
     * 방마다 따로 세면 목록 길이만큼 쿼리가 늘어나므로 한 번에 가져온다.
     */
    @Query("""
            select room, team.teamName, count(member.id)
            from GameRoom room
            join Team team on team.gameRoom = room
            left join TeamMember member on member.team = team and member.isActive = true
            where room.status = :status
            group by room, team.teamName
            order by room.createdAt desc
            """)
    List<Object[]> findRoomSummariesByStatus(@Param("status") GameStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select room
            from GameRoom room
            where room.status = com.kmp.Triply.domain.game.entity.GameStatus.WAITING
              and room.readySinceAt is not null
              and room.readySinceAt <= :cutoff
            """)
    List<GameRoom> findHostDelegationCandidates(@Param("cutoff") LocalDateTime cutoff);
}
