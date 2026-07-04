package com.kmp.Triply.domain.tourism.dto.response;

import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TourismSpotDetailResponse {

    private Long id;
    private String contentId;
    private String name;
    private String category;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String thumbnailUrl;
    private String areaCode;
    private Integer rank;

    public static TourismSpotDetailResponse from(TourismSpot spot) {
        return TourismSpotDetailResponse.builder()
                .id(spot.getId())
                .contentId(spot.getOpenApiContentId())
                .name(spot.getName())
                .category(spot.getCategory() != null ? spot.getCategory().name() : null)
                .address(spot.getAddress())
                .lat(spot.getLat())
                .lng(spot.getLng())
                .thumbnailUrl(spot.getThumbnailUrl())
                .areaCode(spot.getAreaCode())
                .rank(spot.getRank())
                .build();
    }
}