package com.kmp.Triply.domain.user.controller;

import com.kmp.Triply.domain.user.dto.request.UserTravelProfileRequest;
import com.kmp.Triply.domain.user.dto.response.UserTravelProfileResponse;
import com.kmp.Triply.domain.user.service.UserTravelProfileService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Travel Profile", description = "내 여행 프로필 API")
@RestController
@RequestMapping("/api/users/me/travel-profile")
@RequiredArgsConstructor
public class UserTravelProfileController {

    private final UserTravelProfileService userTravelProfileService;

    @Operation(summary = "내 여행 프로필 조회", description = "현재 로그인한 사용자의 여행 프로필을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<UserTravelProfileResponse>> getMyTravelProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(userTravelProfileService.getMyTravelProfile(userId)));
    }

    @Operation(summary = "여행 프로필 저장/재설정", description = "현재 로그인한 사용자의 여행 프로필을 저장합니다. 기존 프로필이 있으면 새 값으로 재설정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserTravelProfileResponse>> saveMyTravelProfile(
            Authentication authentication,
            @Valid @RequestBody UserTravelProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(userTravelProfileService.saveMyTravelProfile(userId, request)));
    }
}