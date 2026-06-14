package com.kmp.Triply.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    private String profileImage;
}