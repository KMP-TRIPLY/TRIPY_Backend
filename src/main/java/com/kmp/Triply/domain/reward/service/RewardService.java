package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;

import java.util.List;

public interface RewardService {

    List<UserRewardResponse> getMyRewards(Long userId);
}