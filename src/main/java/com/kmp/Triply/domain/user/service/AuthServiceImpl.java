package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.entity.RefreshToken;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.RefreshTokenRepository;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshTokenValue = jwtProvider.generateRefreshToken(user.getId());
        long expiresIn = jwtProvider.getRefreshTokenExpiry() / 1000;

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
                .build());

        return TokenResponse.of(accessToken, refreshTokenValue, jwtProvider.getRefreshTokenExpiry() / 1000);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken);
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        User user = savedToken.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user.getId());

        return TokenResponse.of(newAccessToken, refreshToken, jwtProvider.getRefreshTokenExpiry() / 1000);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}