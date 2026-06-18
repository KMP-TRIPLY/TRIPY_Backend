package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserIdOrderByIssuedAtDesc(Long userId);
}
