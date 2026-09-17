package com.kmp.Triply.domain.game.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GameRoomRealtimeNotifierTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final GameRoomRealtimeNotifier notifier = new GameRoomRealtimeNotifier(messagingTemplate);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 트랜잭션이_없으면_바로_보낸다() {
        notifier.publish(7L, "SCORE_UPDATED", "점수 갱신", 120);

        verify(messagingTemplate).convertAndSend(eq("/topic/game-rooms/7"), any(Object.class));
    }

    @Test
    void 트랜잭션_안에서는_커밋된_뒤에_보낸다() {
        TransactionSynchronizationManager.initSynchronization();

        notifier.publish(7L, "SCORE_UPDATED", "점수 갱신", 120);

        // 아직 커밋 전이다. 여기서 보내면 받은 쪽이 조회했을 때 예전 값을 읽는다.
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));

        commit();

        verify(messagingTemplate).convertAndSend(eq("/topic/game-rooms/7"), any(Object.class));
    }

    @Test
    void 롤백되면_아예_보내지_않는다() {
        TransactionSynchronizationManager.initSynchronization();

        notifier.publish(7L, "SCORE_UPDATED", "점수 갱신", 120);
        // 롤백은 afterCommit 을 부르지 않는다. 일어나지 않은 일을 알리지 않는다.
        TransactionSynchronizationManager.clearSynchronization();

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
    }
}
