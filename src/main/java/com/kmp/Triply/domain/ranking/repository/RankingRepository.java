package com.kmp.Triply.domain.ranking.repository;

import com.kmp.Triply.domain.ranking.entity.Ranking;
import com.kmp.Triply.domain.ranking.entity.RankingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    void deleteByGameRoomId(Long gameRoomId);

    List<Ranking> findAllByGameRoomCourseIdAndRankingTypeOrderByFinalScoreDescRankAsc(
            Long courseId,
            RankingType rankingType
    );
}
