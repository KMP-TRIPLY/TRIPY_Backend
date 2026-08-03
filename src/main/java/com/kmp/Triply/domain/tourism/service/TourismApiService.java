package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.domain.tourism.dto.response.NearbyTourismSpotResponse;
import com.kmp.Triply.global.common.PageResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TourismApiService {

    List<RecommendationResponse> getChungcheongRecommendations();

    // region: "충북"(43), "충남"(44), null = 전체
    PageResponse<RecommendationResponse> getChungcheongRecommendations(String region, int page, int size);

    PageResponse<NearbyTourismSpotResponse> getNearbyTourismSpots(
            BigDecimal mapX,
            BigDecimal mapY,
            int radius,
            String contentTypeId,
            int page,
            int size
    );
}
