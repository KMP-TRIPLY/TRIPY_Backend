package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.reward.dto.response.UserRewardResponse;

import java.util.List;

public interface GameRewardGrantService {

    /**
     * 종료된 게임방의 결과로 참가자에게 리워드를 적립한다.
     *
     * @return 이번 호출로 새로 적립된 리워드 (이미 가진 리워드는 포함되지 않는다)
     */
    List<UserRewardResponse> grantForFinishedGame(GameRoom gameRoom);
}
