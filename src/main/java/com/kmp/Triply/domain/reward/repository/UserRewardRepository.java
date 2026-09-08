package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRewardRepository extends JpaRepository<UserReward, Long> {

    List<UserReward> findByUserIdOrderByEarnedAtDesc(Long userId);

    /** (user_id, reward_id) 유니크 제약에 걸리기 전에 이미 가진 업적인지 확인한다. */
    boolean existsByUserIdAndRewardId(Long userId, Long rewardId);
}