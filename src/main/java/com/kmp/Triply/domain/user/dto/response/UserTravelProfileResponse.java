package com.kmp.Triply.domain.user.dto.response;

import com.kmp.Triply.domain.user.entity.CompanionType;
import com.kmp.Triply.domain.user.entity.MoveType;
import com.kmp.Triply.domain.user.entity.StyleType;
import com.kmp.Triply.domain.user.entity.UserTravelProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "여행 프로필 응답")
public class UserTravelProfileResponse {

    @Schema(description = "여행 스타일", example = "HEALING")
    private StyleType styleType;

    @Schema(description = "이동 수단", example = "WALK")
    private MoveType moveType;

    @Schema(description = "동행 유형", example = "FRIENDS")
    private CompanionType companionType;

    @Schema(description = "역사 점수", example = "10")
    private short scoreHistory;

    @Schema(description = "액티비티 점수", example = "20")
    private short scoreAdventure;

    @Schema(description = "음식 점수", example = "15")
    private short scoreFood;

    @Schema(description = "휴양 점수", example = "30")
    private short scoreHealing;

    @Schema(description = "문화 점수", example = "25")
    private short scoreCulture;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static UserTravelProfileResponse from(UserTravelProfile profile) {
        return UserTravelProfileResponse.builder()
                .styleType(profile.getStyleType())
                .moveType(profile.getMoveType())
                .companionType(profile.getCompanionType())
                .scoreHistory(profile.getScoreHistory())
                .scoreAdventure(profile.getScoreAdventure())
                .scoreFood(profile.getScoreFood())
                .scoreHealing(profile.getScoreHealing())
                .scoreCulture(profile.getScoreCulture())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}