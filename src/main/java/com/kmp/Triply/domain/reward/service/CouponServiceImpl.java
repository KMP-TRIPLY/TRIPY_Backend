package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;
import com.kmp.Triply.domain.reward.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private final UserCouponRepository userCouponRepository;

    @Override
    public List<UserCouponResponse> getMyCoupons(Long userId) {
        return userCouponRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}
