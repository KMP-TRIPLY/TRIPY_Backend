package com.kmp.Triply.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {

    @NotBlank(message = "refresh_token은 필수입니다.")
    private String refreshToken;
}