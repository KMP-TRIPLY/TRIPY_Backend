package com.kmp.Triply.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STOMP CONNECT 프레임의 Authorization 헤더로 인증한다.
 * HTTP 시큐리티 필터는 웹소켓 프레임을 타지 않아서, 이게 없으면 누구나 /topic 을 구독할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = JwtProvider.resolveBearer(accessor.getFirstNativeHeader("Authorization"));
        if (token == null || !jwtProvider.validate(token) || tokenBlacklist.contains(token)) {
            throw new MessageDeliveryException("WebSocket 인증 실패");
        }

        accessor.setUser(new UsernamePasswordAuthenticationToken(
                jwtProvider.getUserId(token), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        return message;
    }
}
