package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByTeamId(Long teamId);

    List<TeamMember> findByUserIdOrderByJoinedAtDesc(Long userId);

    /** 내가 아직 활동 중인 멤버로 남아 있고, 아직 끝나지 않은 방. 앱을 다시 켰을 때 돌아갈 곳이다. */
    List<TeamMember> findAllByUserIdAndIsActiveTrueAndTeamGameRoomStatusInOrderByTeamGameRoomCreatedAtDesc(
            Long userId, Collection<GameStatus> statuses);

    Optional<TeamMember> findByTeamGameRoomIdAndUserId(Long gameRoomId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    Optional<TeamMember> findByTeamIdAndUserIdAndIsActiveTrue(Long teamId, Long userId);

    boolean existsByTeamGameRoomIdAndUserId(Long gameRoomId, Long userId);

    List<TeamMember> findAllByTeamIdAndIsActiveTrue(Long teamId);

    List<TeamMember> findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(Long gameRoomId);

    Optional<TeamMember> findByTeamGameRoomIdAndUserIdAndIsActiveTrue(Long gameRoomId, Long userId);

    boolean existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(Long gameRoomId, Long userId);

    /** 이 사람이 지금 게임을 뛰고 있는 방이 있는지. 새 방을 만들지 못하게 막는 데 쓴다. */
    boolean existsByUserIdAndIsActiveTrueAndTeamGameRoomStatus(Long userId, GameStatus status);

    long countByTeamGameRoomId(Long gameRoomId);

    long countByTeamGameRoomIdAndIsActiveTrue(Long gameRoomId);
}
