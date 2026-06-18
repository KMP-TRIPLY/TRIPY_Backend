package com.kmp.Triply.domain.user.dto.request;

import com.kmp.Triply.domain.user.entity.CompanionType;
import com.kmp.Triply.domain.user.entity.MoveType;
import com.kmp.Triply.domain.user.entity.StyleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "여행 프로필 저장/재설정 요청")
public class UserTravelProfileRequest {

    @NotNull(message = "여행 스타일은 필수입니다.")
    @Schema(description = "여행 스타일", example = "HEALING")
    private StyleType styleType;

    @NotNull(message = "이동 수단은 필수입니다.")
    @Schema(description = "이동 수단", example = "WALK")
    private MoveType moveType;

    @NotNull(message = "동행 유형은 필수입니다.")
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
}