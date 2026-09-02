package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoomProgressResponse {

    private Long roomId;
    private String roomName;
    private int totalScore;
    private short hintCountUsed;
    private List<SpotProgressResponse> spots;

    public static RoomProgressResponse of(Team team, List<SpotProgressResponse> spots) {
        return RoomProgressResponse.builder()
                .roomId(team.getGameRoom().getId())
                .roomName(team.getTeamName())
                .totalScore(team.getTotalScore())
                .hintCountUsed(team.getHintCountUsed())
                .spots(spots)
                .build();
    }
}
