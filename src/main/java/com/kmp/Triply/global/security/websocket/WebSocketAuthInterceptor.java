package com.kmp.Triply.global.security.websocket;

import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * STOMP 연결과 구독을 검사한다.
 *
 * <p>핸드셰이크(/ws)는 그대로 열어 둔다. 브라우저 WebSocket 은 헤더를 붙일 수 없어
 * 핸드셰이크 단계에서 토큰을 받기 어렵기 때문이다. 대신 CONNECT 프레임에서 토큰을 받고,
 * SUBSCRIBE 마다 그 방의 팀원인지 확인한다. 이게 없으면 방 번호만 알면 아무나
 * 남의 방 점수와 진행상황을 실시간으로 받아볼 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String GAME_ROOM_TOPIC_PREFIX = "/topic/game-rooms/";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor));
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String token = bearerToken(accessor);
        if (!StringUtils.hasText(token) || !jwtProvider.validate(token)) {
            throw new WebSocketAuthException("연결하려면 유효한 토큰이 필요합니다.");
        }
        User user = userRepository.findById(jwtProvider.getUserId(token))
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new WebSocketAuthException("연결하려면 유효한 토큰이 필요합니다."));

        return new UsernamePasswordAuthenticationToken(
                user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Long userId = userIdOf(accessor);
        if (userId == null) {
            throw new WebSocketAuthException("먼저 연결 인증을 마쳐야 합니다.");
        }
        Long roomId = roomIdOf(accessor.getDestination());
        if (roomId == null) {
            // 방 채널이 아니면 검사할 소속이 없다. 지금은 방 채널만 쓰므로 사실상 닿지 않는다.
            return;
        }
        if (!teamMemberRepository.existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            log.debug("방 {} 구독을 거부했습니다. 팀원이 아닙니다. userId={}", roomId, userId);
            throw new WebSocketAuthException("참여 중인 방만 구독할 수 있습니다.");
        }
    }

    private String bearerToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        return header != null && header.startsWith(BEARER_PREFIX) ? header.substring(BEARER_PREFIX.length()) : null;
    }

    private Long userIdOf(StompHeaderAccessor accessor) {
        return accessor.getUser() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof Long userId ? userId : null;
    }

    /** "/topic/game-rooms/12" 에서 12 를 뽑는다. 하위 경로나 숫자가 아니면 방 채널로 보지 않는다. */
    private Long roomIdOf(String destination) {
        if (destination == null || !destination.startsWith(GAME_ROOM_TOPIC_PREFIX)) {
            return null;
        }
        String suffix = destination.substring(GAME_ROOM_TOPIC_PREFIX.length());
        try {
            return suffix.contains("/") ? null : Long.parseLong(suffix);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
