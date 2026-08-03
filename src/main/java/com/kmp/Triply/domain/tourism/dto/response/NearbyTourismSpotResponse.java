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
@Schema(description = "위치 기반 주변 관광지 응답")
public class NearbyTourismSpotResponse {

    @Schema(description = "관광지 콘텐츠 ID", example = "126508")
    private String contentId;

    @Schema(description = "콘텐츠 타입 ID", example = "12")
    private String contentTypeId;

    @Schema(description = "관광지명", example = "국립공주박물관")
    private String title;

    @Schema(description = "주소", example = "충청남도 공주시 관광단지길 34")
    private String address;

    @Schema(description = "상세 주소", example = "")
    private String addressDetail;

    @Schema(description = "대표 이미지 URL", example = "https://...")
    private String firstImage;

    @Schema(description = "썸네일 이미지 URL", example = "https://...")
    private String firstImageSmall;

    @Schema(description = "경도", example = "127.112345")
    private String mapX;

    @Schema(description = "위도", example = "36.456789")
    private String mapY;

    @Schema(description = "현재 위치로부터 거리(m)", example = "734")
    private int distanceMeters;

    @Schema(description = "지역 코드", example = "34")
    private String areaCode;

    @Schema(description = "시군구 코드", example = "1")
    private String sigunguCode;

    @Schema(description = "대분류 코드", example = "A02")
    private String category1;

    @Schema(description = "중분류 코드", example = "A0201")
    private String category2;

    @Schema(description = "소분류 코드", example = "A02010100")
    private String category3;
}
