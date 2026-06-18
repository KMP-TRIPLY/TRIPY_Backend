package com.kmp.Triply.domain.reward.controller;

import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;
import com.kmp.Triply.domain.reward.service.RewardService;
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

@Tag(name = "Reward", description = "리워드 API")
@RestController
@RequestMapping("/api/users/me/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "내 리워드/배지 목록 조회", description = "현재 로그인한 사용자가 획득한 리워드 및 배지 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserRewardResponse>>> getMyRewards(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(rewardService.getMyRewards(userId)));
    }
}