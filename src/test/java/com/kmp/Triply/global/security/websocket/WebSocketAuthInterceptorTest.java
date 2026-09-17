package com.kmp.Triply.global.security.websocket;

import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 방 번호만 알면 남의 방 점수·진행상황이 실시간으로 새어 나가던 구멍을 막았는지 본다.
 */
class WebSocketAuthInterceptorTest {

    private static final String VALID_TOKEN = "valid-token";
    private static final Long USER_ID = 1L;
    private static final Long ROOM_ID = 7L;

    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
    private final MessageChannel channel = mock(MessageChannel.class);

    private final WebSocketAuthInterceptor interceptor =
            new WebSocketAuthInterceptor(jwtProvider, userRepository, teamMemberRepository);

    @Test
    void 유효한_토큰으로_연결하면_사용자가_붙는다() {
        givenValidToken();
        StompHeaderAccessor accessor = connect("Bearer " + VALID_TOKEN);

        interceptor.preSend(message(accessor), channel);

        assertThat(accessor.getUser()).isNotNull();
        assertThat(((UsernamePasswordAuthenticationToken) accessor.getUser()).getPrincipal()).isEqualTo(USER_ID);
    }

    @Test
    void 토큰이_없거나_틀리면_연결을_막는다() {
        when(jwtProvider.validate("깨진토큰")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message(connect(null)), channel))
                .isInstanceOf(WebSocketAuthException.class);
        assertThatThrownBy(() -> interceptor.preSend(message(connect("Bearer 깨진토큰")), channel))
                .isInstanceOf(WebSocketAuthException.class);
        // Bearer 접두사가 없으면 토큰으로 보지 않는다
        assertThatThrownBy(() -> interceptor.preSend(message(connect(VALID_TOKEN)), channel))
                .isInstanceOf(WebSocketAuthException.class);
    }

    @Test
    void 탈퇴한_사용자는_연결하지_못한다() {
        User deleted = user();
        deleted.softDelete();
        when(jwtProvider.validate(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getUserId(VALID_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> interceptor.preSend(message(connect("Bearer " + VALID_TOKEN)), channel))
                .isInstanceOf(WebSocketAuthException.class);
    }

    @Test
    void 참여_중인_방은_구독할_수_있다() {
        when(teamMemberRepository.existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(ROOM_ID, USER_ID)).thenReturn(true);

        assertThat(interceptor.preSend(message(subscribe("/topic/game-rooms/" + ROOM_ID, USER_ID)), channel))
                .isNotNull();
    }

    @Test
    void 참여하지_않은_방은_구독할_수_없다() {
        when(teamMemberRepository.existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(99L, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message(subscribe("/topic/game-rooms/99", USER_ID)), channel))
                .isInstanceOf(WebSocketAuthException.class);
    }

    @Test
    void 연결_인증_없이는_구독할_수_없다() {
        assertThatThrownBy(() -> interceptor.preSend(message(subscribe("/topic/game-rooms/" + ROOM_ID, null)), channel))
                .isInstanceOf(WebSocketAuthException.class);
    }

    @Test
    void 방_채널이_아닌_목적지는_소속을_따지지_않는다() {
        assertThat(interceptor.preSend(message(subscribe("/topic/notices", USER_ID)), channel)).isNotNull();
        // 하위 경로를 붙여 방 번호 검사를 건너뛰려는 시도도 방 채널로 보지 않는다
        assertThat(interceptor.preSend(message(subscribe("/topic/game-rooms/7/spy", USER_ID)), channel)).isNotNull();
    }

    private void givenValidToken() {
        when(jwtProvider.validate(VALID_TOKEN)).thenReturn(true);
        when(jwtProvider.getUserId(VALID_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    }

    private StompHeaderAccessor connect(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }
        return accessor;
    }

    private StompHeaderAccessor subscribe(String destination, Long userId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        accessor.setDestination(destination);
        if (userId != null) {
            accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
        }
        return accessor;
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private User user() {
        User user = User.builder()
                .email("1@triply.test")
                .nickname("민지")
                .socialProvider(SocialProvider.GOOGLE)
                .socialId("social-1")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }
}
