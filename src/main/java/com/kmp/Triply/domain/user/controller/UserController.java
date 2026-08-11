package com.kmp.Triply.domain.user.controller;

import com.kmp.Triply.domain.user.dto.request.UserUpdateRequest;
import com.kmp.Triply.domain.user.dto.response.UserProfileResponse;
import com.kmp.Triply.domain.user.dto.response.UserResponse;
import com.kmp.Triply.domain.user.service.UserService;
import com.kmp.Triply.global.common.ApiResponse;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 닉네임/프로필 이미지를 수정합니다.")
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(userId, request)));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String bearer) {
        Long userId = (Long) authentication.getPrincipal();
        userService.withdraw(userId, JwtProvider.resolveBearer(bearer));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "사용자 정보 조회", description = "userId에 해당하는 사용자의 기본 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    @Operation(summary = "사용자 프로필 조회", description = "userId에 해당하는 사용자의 공개 프로필 정보를 조회합니다.")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserProfile(userId)));
    }

    @Operation(summary = "사용자 정보 수정", description = "userId에 해당하는 사용자의 닉네임/프로필 이미지를 수정합니다.")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(userId, request)));
    }

    @Operation(summary = "사용자 삭제", description = "userId에 해당하는 사용자를 삭제합니다.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
