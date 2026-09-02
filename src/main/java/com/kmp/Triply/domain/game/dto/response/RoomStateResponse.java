package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamStatus;
import lombok.Builder;
import lombok.Getter;

/** 방의 플레이 상태. 방 하나에 팀 하나이므로 팀 점수가 곧 방 점수다. */
@Getter
@Builder
public class RoomStateResponse {

    private Long roomId;
    private String roomName;
    private TeamStatus playStatus;
    private int totalScore;
    private Short rank;
    private short hintCountUsed;

    public static RoomStateResponse from(Team team) {
        return RoomStateResponse.builder()
                .roomId(team.getGameRoom().getId())
                .roomName(team.getTeamName())
                .playStatus(team.getStatus())
                .totalScore(team.getTotalScore())
                .rank(team.getRank())
                .hintCountUsed(team.getHintCountUsed())
                .build();
    }
}
