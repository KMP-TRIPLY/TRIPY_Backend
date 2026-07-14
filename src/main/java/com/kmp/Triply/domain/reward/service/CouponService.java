package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.reward.dto.request.CouponIssueRequest;
import com.kmp.Triply.domain.reward.dto.response.CouponIssueResponse;
import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;

import java.util.List;

public interface CouponService {

    List<UserCouponResponse> getMyCoupons(Long userId);

    CouponIssueResponse issueCoupon(CouponIssueRequest request);
}
