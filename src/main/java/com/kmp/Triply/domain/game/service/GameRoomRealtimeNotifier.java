package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.response.GameRoomRealtimeEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameRoomRealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long roomId, String eventType, String message, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/game-rooms/" + roomId,
                GameRoomRealtimeEventResponse.of(roomId, eventType, message, payload)
        );
    }
}
