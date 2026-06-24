package com.kmp.Triply.domain.tourism.repository;

import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourismSpotRepository extends JpaRepository<TourismSpot, Long> {

    Optional<TourismSpot> findByOpenApiContentId(String openApiContentId);
}