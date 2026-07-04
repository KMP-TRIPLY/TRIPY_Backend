package com.kmp.Triply.domain.tourism.repository;

import com.kmp.Triply.domain.tourism.entity.TourismApiCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourismApiCacheRepository extends JpaRepository<TourismApiCache, Long> {

    Optional<TourismApiCache> findByContentId(String contentId);
}