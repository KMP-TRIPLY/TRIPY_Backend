package com.kmp.Triply.domain.reward.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 기동 시 리워드 마스터를 등록한다.
 *
 * <p>코스 시드(CourseSeedRunner)와 달리 app.seed.enabled 로 막지 않는다.
 * 코스 시드는 데모 데이터지만 리워드 마스터는 적립 기능이 동작하기 위한 기준 데이터라서,
 * 꺼져 있으면 게임을 끝내도 아무 보상이 안 쌓이는 상태가 조용히 유지된다.
 * {@link RewardSeedService}가 이미 있는 항목은 건너뛰므로 매 기동마다 돌아도 안전하다.
 */
@Component
@RequiredArgsConstructor
public class RewardSeedRunner implements ApplicationRunner {

    private final RewardSeedService rewardSeedService;

    @Override
    public void run(ApplicationArguments args) {
        rewardSeedService.seedIfNeeded();
    }
}
