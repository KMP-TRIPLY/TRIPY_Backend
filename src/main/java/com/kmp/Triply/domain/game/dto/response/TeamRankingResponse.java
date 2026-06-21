package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamRankingResponse {

    private int rank;
    private Long teamId;
    private String teamName;
    private TeamStatus status;
    private int totalScore;
    private short hintCountUsed;

    public static TeamRankingResponse from(Team team, int rank) {
        return TeamRankingResponse.builder()
                .rank(rank)
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .status(team.getStatus())
                .totalScore(team.getTotalScore())
                .hintCountUsed(team.getHintCountUsed())
                .build();
    }
}
