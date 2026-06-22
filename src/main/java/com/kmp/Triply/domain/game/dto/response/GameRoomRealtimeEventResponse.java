package com.kmp.Triply.domain.game.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameRoomRealtimeEventResponse {

    private Long roomId;
    private String eventType;
    private String message;
    private Object payload;
    private LocalDateTime occurredAt;

    public static GameRoomRealtimeEventResponse of(Long roomId, String eventType, String message, Object payload) {
        return GameRoomRealtimeEventResponse.builder()
                .roomId(roomId)
                .eventType(eventType)
                .message(message)
                .payload(payload)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
