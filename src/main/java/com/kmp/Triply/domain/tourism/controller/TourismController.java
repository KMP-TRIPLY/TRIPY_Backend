package com.kmp.Triply.domain.tourism.controller;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.domain.tourism.dto.response.TourismSpotDetailResponse;
import com.kmp.Triply.domain.tourism.service.TourismApiService;
import com.kmp.Triply.domain.tourism.service.TourismSpotService;
import com.kmp.Triply.global.common.ApiResponse;
import com.kmp.Triply.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tourism", description = "관광지 API")
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

    @Operation(summary = "관광지 상세 조회", description = "contentId로 관광지 상세 정보를 반환합니다.")
    @GetMapping("/spots/{contentId}")
    public ResponseEntity<ApiResponse<TourismSpotDetailResponse>> getSpotDetail(
            @Parameter(description = "관광지 콘텐츠 ID", example = "126508")
            @PathVariable String contentId) {
        return ResponseEntity.ok(ApiResponse.ok(tourismSpotService.getSpotDetail(contentId)));
    }
}