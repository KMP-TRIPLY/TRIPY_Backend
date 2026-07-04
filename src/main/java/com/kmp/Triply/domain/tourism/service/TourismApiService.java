package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.global.common.PageResponse;

import java.util.List;

public interface TourismApiService {

    List<RecommendationResponse> getChungcheongRecommendations();

    // region: "충북"(43), "충남"(44), null = 전체
    PageResponse<RecommendationResponse> getChungcheongRecommendations(String region, int page, int size);
}