package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.Team;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameRoomJoinResponse {

    private GameRoomResponse gameRoom;
    private RoomStateResponse state;

    public static GameRoomJoinResponse of(GameRoom gameRoom, Team team) {
        return GameRoomJoinResponse.builder()
                .gameRoom(GameRoomResponse.from(gameRoom))
                .state(RoomStateResponse.from(team))
                .build();
    }
}
