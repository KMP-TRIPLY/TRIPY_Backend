package com.kmp.Triply.domain.reward.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RewardSettlementTeamResponse {

    private Long teamId;
    private String teamName;
    private int rank;
    private int totalScore;
    private int activeMemberScore;
    private int redistributableScore;
    private int redistributedScorePerMember;
    private int redistributionRemainder;
    private List<RewardSettlementMemberResponse> memberSettlements;
    private List<UserCouponResponse> issuedCoupons;

    public static RewardSettlementTeamResponse of(Team team, int rank, int activeMemberScore,
                                                  int redistributableScore, int redistributedScorePerMember,
                                                  int redistributionRemainder,
                                                  List<RewardSettlementMemberResponse> memberSettlements,
                                                  List<UserCouponResponse> issuedCoupons) {
        return RewardSettlementTeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .rank(rank)
                .totalScore(team.getTotalScore())
                .activeMemberScore(activeMemberScore)
                .redistributableScore(redistributableScore)
                .redistributedScorePerMember(redistributedScorePerMember)
                .redistributionRemainder(redistributionRemainder)
                .memberSettlements(memberSettlements)
                .issuedCoupons(issuedCoupons)
                .build();
    }
}
