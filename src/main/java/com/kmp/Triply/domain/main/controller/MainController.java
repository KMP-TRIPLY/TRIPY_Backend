package com.kmp.Triply.domain.main.controller;

import com.kmp.Triply.domain.main.dto.response.DashboardResponse;
import com.kmp.Triply.domain.main.dto.response.DDayResponse;
import com.kmp.Triply.domain.main.service.MainService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Main", description = "메인 홈 API")
@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @Operation(summary = "메인 홈 대시보드 조회", description = "사용자 정보, 가장 가까운 여행 일정, 랜덤 추천 명소 3곳을 반환합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(mainService.getDashboard(userId)));
    }

    @Operation(summary = "여행 D-Day 및 예약 정보 조회", description = "가장 가까운 여행의 D-Day와 상세 정보를 반환합니다.")
    @GetMapping("/d-day")
    public ResponseEntity<ApiResponse<DDayResponse>> getDDay(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(mainService.getDDay(userId)));
    }
}
