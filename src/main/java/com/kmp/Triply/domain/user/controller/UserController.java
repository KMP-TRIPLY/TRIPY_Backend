package com.kmp.Triply.domain.user.controller;

import com.kmp.Triply.domain.user.dto.request.UserUpdateRequest;
import com.kmp.Triply.domain.user.dto.response.UserProfileResponse;
import com.kmp.Triply.domain.user.dto.response.UserResponse;
import com.kmp.Triply.domain.user.service.UserService;
import com.kmp.Triply.global.common.ApiResponse;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "사용자 정보 조회",
            description = "본인의 기본 정보(이메일 포함)를 조회합니다. 다른 사용자는 /{userId}/profile 을 사용하세요.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            Authentication authentication,
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        // UserResponse 에 email 이 들어 있어 공개하면 순차 ID 스캔으로 전 회원 이메일이 털린다
        requireSelf(authentication, userId);
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    @Operation(summary = "사용자 프로필 조회", description = "userId에 해당하는 사용자의 공개 프로필 정보를 조회합니다.")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserProfile(userId)));
    }

    @Operation(summary = "사용자 정보 수정", description = "본인 계정의 닉네임/프로필 이미지를 수정합니다. (POST /me 와 동일)")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            Authentication authentication,
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        requireSelf(authentication, userId);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(userId, request)));
    }

    @Operation(summary = "사용자 삭제", description = "본인 계정을 삭제합니다. (DELETE /me 와 동일)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            Authentication authentication,
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        requireSelf(authentication, userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /** 경로의 userId 가 로그인 주체와 같은지. 없으면 인증된 아무나 남의 계정을 고치고 지운다. */
    private void requireSelf(Authentication authentication, Long userId) {
        if (!userId.equals(authentication.getPrincipal())) {
            throw new CustomException(ErrorCode.USER_ACCESS_DENIED);
        }
    }
}
