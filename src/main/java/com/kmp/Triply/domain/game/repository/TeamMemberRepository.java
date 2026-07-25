package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByTeamId(Long teamId);

    List<TeamMember> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<TeamMember> findByTeamGameRoomIdAndUserId(Long gameRoomId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamGameRoomIdAndUserId(Long gameRoomId, Long userId);

    List<TeamMember> findAllByTeamIdAndIsActiveTrue(Long teamId);

    Optional<TeamMember> findByTeamGameRoomIdAndUserIdAndIsActiveTrue(Long gameRoomId, Long userId);

    boolean existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(Long gameRoomId, Long userId);
}
