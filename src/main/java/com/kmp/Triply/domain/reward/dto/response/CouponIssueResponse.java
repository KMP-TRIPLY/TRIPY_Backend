package com.kmp.Triply.domain.reward.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponIssueResponse {

    private boolean issued;
    private UserCouponResponse coupon;

    public static CouponIssueResponse of(boolean issued, UserCouponResponse coupon) {
        return CouponIssueResponse.builder()
                .issued(issued)
                .coupon(coupon)
                .build();
    }
}
