package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameMode;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameRoomResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long hostUserId;
    private String roomCode;
    private GameStatus status;
    private GameMode gameMode;
    private short maxTeams;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

    public static GameRoomResponse from(GameRoom gameRoom) {
        return GameRoomResponse.builder()
                .id(gameRoom.getId())
                .courseId(gameRoom.getCourse().getId())
                .courseTitle(gameRoom.getCourse().getTitle())
                .hostUserId(gameRoom.getHost().getId())
                .roomCode(gameRoom.getRoomCode())
                .status(gameRoom.getStatus())
                .gameMode(gameRoom.getGameMode())
                .maxTeams(gameRoom.getMaxTeams())
                .startedAt(gameRoom.getStartedAt())
                .endedAt(gameRoom.getEndedAt())
                .createdAt(gameRoom.getCreatedAt())
                .build();
    }
}
