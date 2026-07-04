package com.kmp.Triply.domain.reward.dto.response;

import com.kmp.Triply.domain.reward.entity.RewardType;
import com.kmp.Triply.domain.reward.entity.UserReward;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 리워드/배지 응답")
public class UserRewardResponse {

    @Schema(description = "리워드 ID", example = "1")
    private Long rewardId;

    @Schema(description = "리워드 이름", example = "첫 게임 클리어")
    private String name;

    @Schema(description = "리워드 종류", example = "BADGE")
    private RewardType rewardType;

    @Schema(description = "경험치", example = "100")
    private int expAmount;

    @Schema(description = "설명", example = "첫 게임을 완료했습니다.")
    private String description;

    @Schema(description = "획득일시")
    private LocalDateTime earnedAt;

    public static UserRewardResponse from(UserReward userReward) {
        return UserRewardResponse.builder()
                .rewardId(userReward.getReward().getId())
                .name(userReward.getReward().getName())
                .rewardType(userReward.getReward().getRewardType())
                .expAmount(userReward.getReward().getExpAmount())
                .description(userReward.getReward().getDescription())
                .earnedAt(userReward.getEarnedAt())
                .build();
    }
}