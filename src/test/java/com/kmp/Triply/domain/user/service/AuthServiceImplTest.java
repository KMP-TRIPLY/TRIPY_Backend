package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import com.kmp.Triply.global.security.jwt.RefreshTokenStore;
import com.kmp.Triply.global.security.jwt.TokenBlacklist;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 리프레시 토큰 재발급 분기만 검증한다. Redis 접근은 목으로 대체.
 */
class AuthServiceImplTest {

    private static final Long USER_ID = 7L;
    private static final String SECRET =
            Base64.getEncoder().encodeToString("triply-test-secret-key-32bytes!!".getBytes());

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 60_000L, 600_000L);
    /** 발급 시점부터 이미 만료된 토큰을 만들기 위한 provider (같은 서명키). */
    private final JwtProvider expiredProvider = new JwtProvider(SECRET, -60_000L, -60_000L);

    private final RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
    private final TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);

    private final AuthServiceImpl authService =
            new AuthServiceImpl(jwtProvider, refreshTokenStore, tokenBlacklist);

    @Test
    void 저장된_토큰과_일치하면_액세스_토큰을_재발급한다() {
        String refreshToken = jwtProvider.generateRefreshToken(USER_ID);
        when(refreshTokenStore.matches(USER_ID, refreshToken)).thenReturn(true);

        TokenResponse response = authService.refresh(refreshToken);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(jwtProvider.getUserId(response.getAccessToken())).isEqualTo(USER_ID);
    }

    @Test
    void 저장된_토큰과_다르면_INVALID() {
        String refreshToken = jwtProvider.generateRefreshToken(USER_ID);
        when(refreshTokenStore.matches(USER_ID, refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 만료된_토큰이면_EXPIRED() {
        String expired = expiredProvider.generateRefreshToken(USER_ID);

        assertThatThrownBy(() -> authService.refresh(expired))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void 서명이_틀리면_INVALID() {
        String otherSecret = Base64.getEncoder().encodeToString("another-test-secret-key-32bytes!!".getBytes());
        String forged = new JwtProvider(otherSecret, 60_000L, 600_000L).generateRefreshToken(USER_ID);

        assertThatThrownBy(() -> authService.refresh(forged))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 로그아웃하면_리프레시_삭제와_액세스_블랙리스트가_함께_일어난다() {
        String accessToken = jwtProvider.generateAccessToken(USER_ID);

        authService.logout(USER_ID, accessToken);

        verify(refreshTokenStore).delete(USER_ID);
        verify(tokenBlacklist).add(accessToken);
    }

    @Test
    void 토큰_발급시_리프레시_토큰이_저장된다() {
        com.kmp.Triply.domain.user.entity.User user = mock(com.kmp.Triply.domain.user.entity.User.class);
        when(user.getId()).thenReturn(USER_ID);

        TokenResponse response = authService.issueTokens(user);

        verify(refreshTokenStore).save(eq(USER_ID), eq(response.getRefreshToken()), any());
    }
}
