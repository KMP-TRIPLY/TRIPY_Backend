package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.response.GameRoomRealtimeEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class GameRoomRealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 방 채널로 이벤트를 보낸다. 트랜잭션 안에서 불리면 커밋된 뒤에 나간다.
     *
     * <p>바로 보내면 두 가지가 깨진다. 받은 쪽이 곧장 조회 API 를 부르면 아직 커밋 전이라
     * 예전 값을 읽고, 뒤이어 롤백이라도 나면 일어나지도 않은 일을 알린 셈이 된다.
     * 커밋 뒤로 미루면 "알림이 왔다 = 서버에 반영됐다" 가 항상 참이 된다.
     */
    public void publish(Long roomId, String eventType, String message, Object payload) {
        GameRoomRealtimeEventResponse event =
                GameRoomRealtimeEventResponse.of(roomId, eventType, message, payload);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(roomId, event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(roomId, event);
            }
        });
    }

    private void send(Long roomId, GameRoomRealtimeEventResponse event) {
        messagingTemplate.convertAndSend("/topic/game-rooms/" + roomId, event);
    }
}
