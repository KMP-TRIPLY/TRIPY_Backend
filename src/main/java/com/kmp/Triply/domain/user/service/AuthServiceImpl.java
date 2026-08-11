package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import com.kmp.Triply.global.security.jwt.RefreshTokenStore;
import com.kmp.Triply.global.security.jwt.TokenBlacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenStore.save(user.getId(), refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry()));

        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getRefreshTokenExpiry() / 1000);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.validate(refreshToken)) {
            throw new CustomException(jwtProvider.isExpired(refreshToken)
                    ? ErrorCode.EXPIRED_REFRESH_TOKEN
                    : ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // 로그아웃했거나, 다른 기기에서 재로그인해 교체된 토큰
        if (!refreshTokenStore.matches(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return TokenResponse.of(jwtProvider.generateAccessToken(userId), refreshToken,
                jwtProvider.getRefreshTokenExpiry() / 1000);
    }

    @Override
    public void logout(Long userId, String accessToken) {
        refreshTokenStore.delete(userId);
        // 액세스 토큰은 만료 전까지 그대로 유효하므로 블랙리스트에 넣어야 실제로 끊긴다
        tokenBlacklist.add(accessToken);
    }
}
