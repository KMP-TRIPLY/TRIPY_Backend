package com.kmp.Triply.domain.reward.dto.response;

import com.kmp.Triply.domain.game.entity.TeamMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardSettlementMemberResponse {

    private Long userId;
    private String nickname;
    private int originalScore;
    private int redistributedScore;
    private int finalScore;

    public static RewardSettlementMemberResponse of(TeamMember teamMember, int originalScore, int redistributedScore) {
        return RewardSettlementMemberResponse.builder()
                .userId(teamMember.getUser().getId())
                .nickname(teamMember.getUser().getNickname())
                .originalScore(originalScore)
                .redistributedScore(redistributedScore)
                .finalScore(originalScore + redistributedScore)
                .build();
    }
}
