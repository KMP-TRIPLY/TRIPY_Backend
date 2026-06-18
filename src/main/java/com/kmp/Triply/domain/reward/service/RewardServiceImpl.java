package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;
import com.kmp.Triply.domain.reward.repository.UserRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardServiceImpl implements RewardService {

    private final UserRewardRepository userRewardRepository;

    @Override
    public List<UserRewardResponse> getMyRewards(Long userId) {
        return userRewardRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(UserRewardResponse::from)
                .toList();
    }
}
