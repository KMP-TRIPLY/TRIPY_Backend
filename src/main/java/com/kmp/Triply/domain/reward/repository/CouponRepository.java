package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findAllByIsActiveTrueOrderByValidUntilAsc();
}
