package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.entity.User;

public interface AuthService {

    TokenResponse issueTokens(User user);

    TokenResponse refresh(String refreshToken);

    void logout(Long userId, String accessToken);
}