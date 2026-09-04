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
    /** 방 이름. 방 코드는 이제 어디에도 노출되지 않아 리포트에서 알아볼 수 없다. */
    private String roomName;
    private boolean settled;
    private int teamCount;
    private int issuedCouponCount;
    private LocalDateTime settledAt;
    private List<RewardSettlementTeamResponse> teams;

    public static RewardSettlementResponse of(GameRoom gameRoom, int issuedCouponCount,
                                              List<RewardSettlementTeamResponse> teams) {
        return RewardSettlementResponse.builder()
                .gameRoomId(gameRoom.getId())
                // 한 방 = 한 팀이라 팀 이름이 곧 방 이름이다.
                .roomName(teams.isEmpty() ? null : teams.get(0).getTeamName())
                .settled(true)
                .teamCount(teams.size())
                .issuedCouponCount(issuedCouponCount)
                .settledAt(LocalDateTime.now())
                .teams(teams)
                .build();
    }
}
