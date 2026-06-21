package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByGameRoomId(Long gameRoomId);

    List<Team> findAllByGameRoomIdOrderByTotalScoreDescCreatedAtAsc(Long gameRoomId);

    long countByGameRoomId(Long gameRoomId);
}
