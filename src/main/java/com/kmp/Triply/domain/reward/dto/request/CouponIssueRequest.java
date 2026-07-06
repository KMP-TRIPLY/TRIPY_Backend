package com.kmp.Triply.domain.reward.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CouponIssueRequest {

    @NotNull(message = "쿠폰 ID는 필수입니다.")
    private Long couponId;

    @NotNull(message = "발급 대상 사용자 ID는 필수입니다.")
    private Long userId;

    private Long gameRoomId;
}
