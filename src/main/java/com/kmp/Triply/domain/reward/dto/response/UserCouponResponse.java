package com.kmp.Triply.domain.reward.dto.response;

import com.kmp.Triply.domain.reward.entity.BarcodeType;
import com.kmp.Triply.domain.reward.entity.CouponStatus;
import com.kmp.Triply.domain.reward.entity.DiscountType;
import com.kmp.Triply.domain.reward.entity.UserCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 쿠폰 응답")
public class UserCouponResponse {

    @Schema(description = "사용자 쿠폰 ID", example = "1")
    private Long id;

    @Schema(description = "쿠폰 코드", example = "TRIPLY-AB12CD")
    private String couponCode;

    @Schema(description = "쿠폰 제목", example = "강릉 카페 10% 할인")
    private String title;

    @Schema(description = "제휴사명", example = "테라로사")
    private String partnerName;

    @Schema(description = "제휴 지역", example = "강릉")
    private String partnerRegion;

    @Schema(description = "할인 종류", example = "PERCENT")
    private DiscountType discountType;

    @Schema(description = "할인 값", example = "10")
    private int discountValue;

    @Schema(description = "바코드 종류", example = "QR")
    private BarcodeType barcodeType;

    @Schema(description = "쿠폰 상태", example = "ISSUED")
    private CouponStatus status;

    @Schema(description = "발급일시")
    private LocalDateTime issuedAt;

    @Schema(description = "사용일시")
    private LocalDateTime usedAt;

    @Schema(description = "만료일시")
    private LocalDateTime expiresAt;

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .couponCode(userCoupon.getCouponCode())
                .title(userCoupon.getCoupon().getTitle())
                .partnerName(userCoupon.getCoupon().getPartnerName())
                .partnerRegion(userCoupon.getCoupon().getPartnerRegion())
                .discountType(userCoupon.getCoupon().getDiscountType())
                .discountValue(userCoupon.getCoupon().getDiscountValue())
                .barcodeType(userCoupon.getCoupon().getBarcodeType())
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .usedAt(userCoupon.getUsedAt())
                .expiresAt(userCoupon.getExpiresAt())
                .build();
    }
}
