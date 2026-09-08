package com.kmp.Triply.domain.reward.seed;

import com.kmp.Triply.domain.reward.RewardCatalog;
import com.kmp.Triply.domain.reward.entity.Reward;
import com.kmp.Triply.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * {@link RewardCatalog} 의 리워드 마스터를 {@code rewards} 테이블에 등록한다.
 *
 * <p>이름으로 존재 여부를 보므로 몇 번 돌려도 안전하고, 카탈로그에 항목을 추가한 뒤
 * 재기동하면 빠진 것만 채워진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardSeedService {

    private final RewardRepository rewardRepository;

    @Transactional
    public void seedIfNeeded() {
        List<Reward> missing = Arrays.stream(RewardCatalog.values())
                .filter(definition -> !rewardRepository.existsByName(definition.getRewardName()))
                .map(definition -> Reward.builder()
                        .name(definition.getRewardName())
                        .rewardType(definition.getRewardType())
                        .expAmount(definition.getExpAmount())
                        .description(definition.getDescription())
                        .build())
                .toList();

        if (missing.isEmpty()) {
            log.info("리워드 마스터 {}건이 모두 등록되어 있습니다. 시딩을 건너뜁니다.", RewardCatalog.values().length);
            return;
        }

        rewardRepository.saveAll(missing);
        log.info("리워드 마스터 {}건을 등록했습니다: {}", missing.size(),
                missing.stream().map(Reward::getName).toList());
    }
}
