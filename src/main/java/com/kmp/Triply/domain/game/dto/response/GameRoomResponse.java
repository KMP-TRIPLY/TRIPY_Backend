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
    private GameStatus status;
    private GameMode gameMode;
    private short maxMembers;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime readySinceAt;
    private LocalDateTime createdAt;

    public static GameRoomResponse from(GameRoom gameRoom) {
        return GameRoomResponse.builder()
                .id(gameRoom.getId())
                .courseId(gameRoom.getCourse().getId())
                .courseTitle(gameRoom.getCourse().getTitle())
                .hostUserId(gameRoom.getHost().getId())
                .status(gameRoom.getStatus())
                .gameMode(gameRoom.getGameMode())
                .maxMembers(gameRoom.getMaxMembers())
                .startedAt(gameRoom.getStartedAt())
                .endedAt(gameRoom.getEndedAt())
                .readySinceAt(gameRoom.getReadySinceAt())
                .createdAt(gameRoom.getCreatedAt())
                .build();
    }
}
