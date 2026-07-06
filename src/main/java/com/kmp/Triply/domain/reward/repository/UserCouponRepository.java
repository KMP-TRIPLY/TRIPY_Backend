package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserIdOrderByIssuedAtDesc(Long userId);

    Optional<UserCoupon> findByCouponIdAndUserIdAndGameRoomId(Long couponId, Long userId, Long gameRoomId);

    boolean existsByCouponCode(String couponCode);

    long countByCouponId(Long couponId);
}
