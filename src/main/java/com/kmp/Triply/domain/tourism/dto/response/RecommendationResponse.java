package com.kmp.Triply.domain.tourism.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "중심관광지 추천 목록 응답 (한국관광공사 TourAPI 기반)")
public class RecommendationResponse {

    @Schema(description = "관광지 콘텐츠 ID (TourAPI hubTatsCd)", example = "126508")
    private String contentId;       // hubTatsCd

    @Schema(description = "관광지명 (TourAPI hubTatsNm)", example = "대전 오월드")
    private String title;           // hubTatsNm

    @Schema(description = "경도 (X 좌표)", example = "127.3845")
    private String mapX;

    @Schema(description = "위도 (Y 좌표)", example = "36.2954")
    private String mapY;

    @Schema(description = "지역(시도) 코드", example = "3")
    private String areaCd;

    @Schema(description = "지역(시도)명", example = "대전")
    private String areaNm;

    @Schema(description = "시군구 코드", example = "1")
    private String signguCd;

    @Schema(description = "시군구명", example = "중구")
    private String signguNm;

    @Schema(description = "카테고리 대분류 (TourAPI hubCtgryLclsNm)", example = "자연")
    private String categoryLarge;   // hubCtgryLclsNm

    @Schema(description = "카테고리 중분류 (TourAPI hubCtgryMclsNm)", example = "자연관광지")
    private String categoryMiddle;  // hubCtgryMclsNm

    @Schema(description = "중심관광지 순위 (TourAPI hubRank)", example = "1")
    private int rank;               // hubRank

    @Schema(description = "대표 사진 URL. 통계 API 는 사진을 주지 않아 국문 관광정보에서 찾아 채운다. "
            + "이름이 일치하는 곳을 못 찾으면 null",
            example = "http://tong.visitkorea.or.kr/cms/resource/33/3534933_image2_1.jpg")
    private String imageUrl;

    /** 사진은 통계 API 응답에 없어 뒤늦게 채워진다. */
    public void applyImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
