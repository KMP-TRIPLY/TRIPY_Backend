package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {

    List<UserReward> findByUserIdOrderByEarnedAtDesc(Long userId);
}