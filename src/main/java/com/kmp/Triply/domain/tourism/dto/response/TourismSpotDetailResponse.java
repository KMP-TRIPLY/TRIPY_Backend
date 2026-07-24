package com.kmp.Triply.domain.tourism.dto.response;

import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "관광지 상세 정보 응답")
public class TourismSpotDetailResponse {

    @Schema(description = "관광지 PK (내부 DB ID)", example = "1")
    private Long id;

    @Schema(description = "관광지 콘텐츠 ID (TourAPI hubTatsCd)", example = "126508")
    private String contentId;

    @Schema(description = "관광지명", example = "대전 오월드")
    private String name;

    @Schema(description = "카테고리 (HERITAGE, NATURE, FOOD, FESTIVAL, SHOP)", example = "NATURE")
    private String category;

    @Schema(description = "카테고리 대분류 (TourAPI hubCtgryLclsNm)", example = "자연")
    private String categoryLarge;

    @Schema(description = "카테고리 중분류 (TourAPI hubCtgryMclsNm)", example = "자연관광지")
    private String categoryMiddle;

    @Schema(description = "주소", example = "대전광역시 중구 사정공원로 70")
    private String address;

    @Schema(description = "위도", example = "36.2954")
    private BigDecimal lat;

    @Schema(description = "경도", example = "127.3845")
    private BigDecimal lng;

    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.triply.com/spots/126508.jpg")
    private String thumbnailUrl;

    @Schema(description = "지역(시도) 코드", example = "3")
    private String areaCode;

    @Schema(description = "중심관광지 순위 (TourAPI hubRank)", example = "1")
    private Integer rank;

    public static TourismSpotDetailResponse from(TourismSpot spot) {
        return TourismSpotDetailResponse.builder()
                .id(spot.getId())
                .contentId(spot.getOpenApiContentId())
                .name(spot.getName())
                .category(spot.getCategory() != null ? spot.getCategory().name() : null)
                .categoryLarge(spot.getCategoryLarge())
                .categoryMiddle(spot.getCategoryMiddle())
                .address(spot.getAddress())
                .lat(spot.getLat())
                .lng(spot.getLng())
                .thumbnailUrl(spot.getThumbnailUrl())
                .areaCode(spot.getAreaCode())
                .rank(spot.getRank())
                .build();
    }
}
