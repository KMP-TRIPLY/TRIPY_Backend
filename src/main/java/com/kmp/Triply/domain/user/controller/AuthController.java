package com.kmp.Triply.domain.user.controller;

import com.kmp.Triply.domain.user.dto.request.TokenRefreshRequest;
import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.service.AuthService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = """
        인증/인가 API

        소셜 로그인은 Spring Security OAuth2Login 필터가 처리하며 아래 진입점은 Swagger에 노출되지 않습니다. 브라우저로 직접 접근하세요.
        - GET /oauth2/authorization/kakao
        - GET /oauth2/authorization/naver
        - GET /oauth2/authorization/google

        로그인 성공 시 {app.oauth2.redirect-uri}?access_token=...&refresh_token=... 로 리다이렉트됩니다.
        """)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 새 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request.getRefreshToken())));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 리프레시 토큰을 만료시킵니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}