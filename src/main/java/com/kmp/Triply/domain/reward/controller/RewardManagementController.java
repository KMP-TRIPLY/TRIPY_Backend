package com.kmp.Triply.domain.reward.controller;

import com.kmp.Triply.domain.reward.dto.request.CouponIssueRequest;
import com.kmp.Triply.domain.reward.dto.request.RewardSettleRequest;
import com.kmp.Triply.domain.reward.dto.response.CouponIssueResponse;
import com.kmp.Triply.domain.reward.dto.response.RewardSettlementResponse;
import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;
import com.kmp.Triply.domain.reward.service.CouponService;
import com.kmp.Triply.domain.reward.service.RewardService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reward Management", description = "리워드 정산 및 쿠폰 발급 API")
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardManagementController {

    private final RewardService rewardService;
    private final CouponService couponService;

    @Operation(summary = "게임 결과 정산", description = "종료된 게임룸의 팀 순위 기준으로 보상 정산 리포트를 생성하고 쿠폰을 발급합니다.")
    @PostMapping("/settle")
    public ResponseEntity<ApiResponse<RewardSettlementResponse>> settleRewards(
            @Valid @RequestBody RewardSettleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(rewardService.settleRewards(request)));
    }

    @Operation(summary = "제휴처 쿠폰 발급", description = "제휴처 쿠폰을 지정한 사용자에게 발급합니다.")
    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CouponIssueResponse>> issueCoupon(
            @Valid @RequestBody CouponIssueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(couponService.issueCoupon(request)));
    }

    @Operation(summary = "내 쿠폰함 리스트 조회", description = "현재 로그인한 사용자의 쿠폰함 목록을 조회합니다.")
    @GetMapping("/coupons/my")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyCoupons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(couponService.getMyCoupons(userId)));
    }
}
