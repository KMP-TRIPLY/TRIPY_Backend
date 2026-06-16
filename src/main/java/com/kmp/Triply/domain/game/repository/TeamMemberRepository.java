package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByUserIdOrderByJoinedAtDesc(Long userId);
}
