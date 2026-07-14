package com.kmp.Triply.domain.reward.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RewardSettleRequest {

    @NotNull(message = "게임룸 ID는 필수입니다.")
    private Long gameRoomId;
}
