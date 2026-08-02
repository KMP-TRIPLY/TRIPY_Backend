package com.kmp.Triply.domain.tourism.controller;

import com.kmp.Triply.domain.tourism.dto.response.NearbyTourismSpotResponse;
import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.domain.tourism.dto.response.TourismSpotDetailResponse;
import com.kmp.Triply.domain.tourism.service.TourismApiService;
import com.kmp.Triply.domain.tourism.service.TourismSpotService;
import com.kmp.Triply.global.common.ApiResponse;
import com.kmp.Triply.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Tourism", description = "관광지 API")
@Validated
@RestController
@RequestMapping("/api/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TourismApiService tourismApiService;
    private final TourismSpotService tourismSpotService;

    @Operation(summary = "충청권 중심관광지 목록 조회",
            description = "region 파라미터로 필터링 가능. 충북 / 충남 / 대전 / 세종 / 생략 시 전체. page(0-based), size로 페이징.")
    @GetMapping("/spots")
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> getSpots(
            @Parameter(description = "지역 필터 (충북, 충남, 대전, 세종 등) — 생략 시 전체", example = "대전")
            @RequestParam(required = false) String region,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(tourismApiService.getChungcheongRecommendations(region, page, size)));
    }

    @Operation(summary = "GPS 기반 주변 관광지 조회",
            description = "한국관광공사 국문 관광정보 서비스 GW의 locationBasedList2를 사용해 현재 위치 주변 관광지를 거리순으로 조회합니다.")
    @GetMapping("/spots/nearby")
    public ResponseEntity<ApiResponse<PageResponse<NearbyTourismSpotResponse>>> getNearbySpots(
            @Parameter(description = "현재 위치 경도 X 좌표", example = "127.1193983")
            @RequestParam BigDecimal mapX,
            @Parameter(description = "현재 위치 위도 Y 좌표", example = "36.4655023")
            @RequestParam BigDecimal mapY,
            @Parameter(description = "검색 반경(m). 최대 20000m 권장", example = "1000")
            @Min(1) @Max(20000) @RequestParam(defaultValue = "1000") int radius,
            @Parameter(description = "관광 타입 ID. 관광지=12, 문화시설=14, 행사/공연/축제=15, 여행코스=25, 레포츠=28, 숙박=32, 쇼핑=38, 음식점=39", example = "12")
            @RequestParam(required = false) String contentTypeId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 항목 수", example = "20")
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                tourismApiService.getNearbyTourismSpots(mapX, mapY, radius, contentTypeId, page, size)
        ));
    }

    @Operation(summary = "관광지 상세 조회", description = "contentId로 관광지 상세 정보를 반환합니다.")
    @GetMapping("/spots/{contentId}")
    public ResponseEntity<ApiResponse<TourismSpotDetailResponse>> getSpotDetail(
            @Parameter(description = "관광지 콘텐츠 ID", example = "126508")
            @PathVariable String contentId) {
        return ResponseEntity.ok(ApiResponse.ok(tourismSpotService.getSpotDetail(contentId)));
    }
}
