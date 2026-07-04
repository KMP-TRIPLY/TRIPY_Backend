package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.TourismSpotDetailResponse;

public interface TourismSpotService {

    TourismSpotDetailResponse getSpotDetail(String contentId);
}
