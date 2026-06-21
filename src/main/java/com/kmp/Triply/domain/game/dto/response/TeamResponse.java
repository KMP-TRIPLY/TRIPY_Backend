package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamResponse {

    private Long id;
    private Long gameRoomId;
    private Long leaderUserId;
    private String teamName;
    private TeamStatus status;
    private int totalScore;
    private Short rank;
    private short hintCountUsed;

    public static TeamResponse from(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .gameRoomId(team.getGameRoom().getId())
                .leaderUserId(team.getLeader().getId())
                .teamName(team.getTeamName())
                .status(team.getStatus())
                .totalScore(team.getTotalScore())
                .rank(team.getRank())
                .hintCountUsed(team.getHintCountUsed())
                .build();
    }
}
