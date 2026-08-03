package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameProgressRepository extends JpaRepository<GameProgress, Long> {

    Optional<GameProgress> findByTeamIdAndCourseSpotId(Long teamId, Long courseSpotId);

    List<GameProgress> findAllByTeamId(Long teamId);
}