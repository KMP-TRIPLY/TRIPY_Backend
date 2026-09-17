package com.kmp.Triply.global.security.websocket;

import org.springframework.messaging.MessagingException;

/** STOMP 연결·구독이 거부됐을 때. 클라이언트에는 ERROR 프레임으로 전달된다. */
public class WebSocketAuthException extends MessagingException {

    public WebSocketAuthException(String message) {
        super(message);
    }
}
