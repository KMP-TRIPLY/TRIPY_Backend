package com.kmp.Triply.domain.reward.dto.response;

import com.kmp.Triply.domain.game.entity.GameRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RewardSettlementResponse {

    private Long gameRoomId;
    private String roomCode;
    private boolean settled;
    private int teamCount;
    private int issuedCouponCount;
    private LocalDateTime settledAt;
    private List<RewardSettlementTeamResponse> teams;

    public static RewardSettlementResponse of(GameRoom gameRoom, int issuedCouponCount,
                                              List<RewardSettlementTeamResponse> teams) {
        return RewardSettlementResponse.builder()
                .gameRoomId(gameRoom.getId())
                .roomCode(gameRoom.getRoomCode())
                .settled(true)
                .teamCount(teams.size())
                .issuedCouponCount(issuedCouponCount)
                .settledAt(LocalDateTime.now())
                .teams(teams)
                .build();
    }
}
