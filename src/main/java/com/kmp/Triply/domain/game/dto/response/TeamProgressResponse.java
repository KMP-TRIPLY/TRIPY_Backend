package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamProgressResponse {

    private Long teamId;
    private String teamName;
    private int totalScore;
    private short hintCountUsed;
    private List<SpotProgressResponse> spots;

    public static TeamProgressResponse of(Team team, List<SpotProgressResponse> spots) {
        return TeamProgressResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .totalScore(team.getTotalScore())
                .hintCountUsed(team.getHintCountUsed())
                .spots(spots)
                .build();
    }
}
