package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.domain.tourism.dto.response.NearbyTourismSpotResponse;
import com.kmp.Triply.global.common.PageResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TourismApiService {

    List<RecommendationResponse> getChungcheongRecommendations();

    /**
     * 스팟 사진 URL. 통계 API(LocgoHubTarService1)는 사진을 주지 않으므로
     * 국문 관광정보(KorService2)에서 좌표·이름으로 찾아 tourism_spots 에 저장하고 재사용한다.
     * 못 찾으면 빈 값 — 엉뚱한 사진을 붙이는 것보다 없는 게 낫다.
     */
    Optional<String> findThumbnailUrl(String openApiContentId);

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
