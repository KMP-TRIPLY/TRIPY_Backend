package com.kmp.Triply.domain.reward.controller;

import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;
import com.kmp.Triply.domain.reward.service.CouponService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Coupon", description = "쿠폰 API")
@RestController
@RequestMapping("/api/users/me/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "내 쿠폰함 조회", description = "현재 로그인한 사용자가 보유한 쿠폰 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyCoupons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(couponService.getMyCoupons(userId)));
    }
}