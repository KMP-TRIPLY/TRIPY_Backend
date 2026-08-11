package com.kmp.Triply.global.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CONNECT 프레임 인증 분기만 검증한다.
 */
class StompAuthChannelInterceptorTest {

    private static final Long USER_ID = 7L;
    private static final String SECRET =
            Base64.getEncoder().encodeToString("triply-test-secret-key-32bytes!!".getBytes());

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 60_000L, 600_000L);
    private final TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
    private final StompAuthChannelInterceptor interceptor =
            new StompAuthChannelInterceptor(jwtProvider, tokenBlacklist);

    private static Message<byte[]> connectFrame(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        // 실제 STOMP 핸들러도 인터셉터가 헤더를 고칠 수 있도록 mutable 로 넘긴다
        accessor.setLeaveMutable(true);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void 유효한_토큰이면_사용자가_붙는다() {
        String token = jwtProvider.generateAccessToken(USER_ID);
        when(tokenBlacklist.contains(token)).thenReturn(false);

        Message<?> result = interceptor.preSend(connectFrame("Bearer " + token), null);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor).isNotNull();
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo(USER_ID.toString());
    }

    @Test
    void 토큰이_없으면_거절한다() {
        assertThatThrownBy(() -> interceptor.preSend(connectFrame(null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void 서명이_틀리면_거절한다() {
        String otherSecret = Base64.getEncoder().encodeToString("another-test-secret-key-32bytes!!".getBytes());
        String forged = new JwtProvider(otherSecret, 60_000L, 600_000L).generateAccessToken(USER_ID);

        assertThatThrownBy(() -> interceptor.preSend(connectFrame("Bearer " + forged), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void 로그아웃된_토큰이면_거절한다() {
        String token = jwtProvider.generateAccessToken(USER_ID);
        when(tokenBlacklist.contains(token)).thenReturn(true);

        assertThatThrownBy(() -> interceptor.preSend(connectFrame("Bearer " + token), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void CONNECT_가_아닌_프레임은_그대로_통과시킨다() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> frame = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThat(interceptor.preSend(frame, null)).isSameAs(frame);
    }
}
