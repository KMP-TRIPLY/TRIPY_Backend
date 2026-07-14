package com.kmp.Triply.domain.ranking.repository;

import com.kmp.Triply.domain.ranking.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    List<Ranking> findAllByGameRoomCourseIdOrderByRankAsc(Long courseId);
}
