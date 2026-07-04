package com.kmp.Triply.domain.user.dto.response;

import com.kmp.Triply.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 공개 프로필 응답")
public class UserProfileResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "닉네임", example = "여행가짱")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.triply.com/profile/1.png")
    private String profileImage;

    @Schema(description = "사용자 레벨", example = "3")
    private int level;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImg())
                .level(user.getLevel())
                .build();
    }
}