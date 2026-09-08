package com.kmp.Triply.domain.reward;

import com.kmp.Triply.domain.reward.entity.RewardType;
import lombok.Getter;

/**
 * 지급 가능한 리워드 목록. 시드(등록)와 적립(지급)이 같은 정의를 보므로 이름이 어긋날 일이 없다.
 *
 * <p>{@code user_rewards} 는 (user_id, reward_id) 유니크라서 리워드마다 유저당 한 번만 쌓인다.
 * 그래서 "매 게임 참가 보상" 같은 반복 적립은 이 스키마로 표현할 수 없고,
 * 여기 있는 항목은 모두 <b>최초 달성 시 한 번</b> 주는 업적이다.
 */
@Getter
public enum RewardCatalog {

    FIRST_JOURNEY("첫 여행의 시작", RewardType.BADGE, 50,
            "첫 게임을 완주했습니다."),
    FIRST_WIN("첫 승리", RewardType.BADGE, 100,
            "게임에서 1위로 완주했습니다."),
    NO_HINT_CLEAR("노힌트 클리어", RewardType.BADGE, 80,
            "힌트를 쓰지 않고 미션을 해결했습니다."),
    MISSION_SOLVER("미션 해결사", RewardType.TITLE, 150,
            "한 게임에서 미션을 2개 이상 해결했습니다.");

    /** enum 의 name() 과 겹치지 않도록 rewardName 으로 둔다. rewards.name 컬럼과 매칭되는 값이다. */
    private final String rewardName;
    private final RewardType rewardType;
    private final int expAmount;
    private final String description;

    RewardCatalog(String rewardName, RewardType rewardType, int expAmount, String description) {
        this.rewardName = rewardName;
        this.rewardType = rewardType;
        this.expAmount = expAmount;
        this.description = description;
    }
}
