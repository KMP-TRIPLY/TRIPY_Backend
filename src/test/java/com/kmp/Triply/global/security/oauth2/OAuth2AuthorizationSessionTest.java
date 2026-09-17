package com.kmp.Triply.global.security.oauth2;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 로그인 시작(Redis 저장) → 콜백(Redis 복원) 한 바퀴를 돌려
 * 콜백 주소 선택과 초대 문맥 전달을 검증한다.
 */
class OAuth2AuthorizationSessionTest {

    private static final String DEFAULT_URI = "https://triply-six.vercel.app/oauth2/callback";
    private static final String QA_URI = "https://triply-qa.onrender.com";
    private static final String STATE = "state-abc";

    private final Map<String, String> redis = new HashMap<>();
    private final RedisOAuth2AuthorizationRequestRepository repository = repository();
    private final OAuth2AuthenticationSuccessHandler handler = handler();

    @Test
    void 목록에_있는_콜백을_요청하면_그_주소로_보낸다() {
        assertThat(handler.resolveRedirectUri(cycle(QA_URI, null))).isEqualTo(QA_URI);
        assertThat(handler.resolveRedirectUri(cycle(DEFAULT_URI, null))).isEqualTo(DEFAULT_URI);
    }

    @Test
    void 요청이_없으면_기본_콜백을_쓴다() {
        assertThat(handler.resolveRedirectUri(cycle(null, null))).isEqualTo(DEFAULT_URI);
        assertThat(handler.resolveRedirectUri(cycle("   ", null))).isEqualTo(DEFAULT_URI);
        assertThat(handler.resolveRedirectUri(null)).isEqualTo(DEFAULT_URI);
    }

    @Test
    void 목록에_없는_주소는_무시하고_기본_콜백을_쓴다() {
        assertThat(handler.resolveRedirectUri(cycle("https://evil.example.com/steal", null))).isEqualTo(DEFAULT_URI);
        // 부분 일치로 뚫리지 않아야 한다
        assertThat(handler.resolveRedirectUri(cycle(QA_URI + ".evil.com", null))).isEqualTo(DEFAULT_URI);
        assertThat(handler.resolveRedirectUri(cycle("https://triply-qa.onrender.com.evil.com", null))).isEqualTo(DEFAULT_URI);
    }

    @Test
    void 브라우저가_바뀌어도_초대_방_번호가_콜백까지_따라온다() {
        // 콜백 요청에는 쿠키도 localStorage 도 없다. state 하나로 복원해야 한다.
        OAuth2AuthorizationSession session = cycle(QA_URI, "42");

        assertThat(session).isNotNull();
        assertThat(session.inviteRoomId()).isEqualTo(42L);
        assertThat(session.registrationId()).isEqualTo("google");
        assertThat(session.authorizationRequest().getState()).isEqualTo(STATE);
    }

    @Test
    void 초대_로그인이면_리다이렉트에_방_번호가_실린다() throws Exception {
        String target = redirectUrlAfterLogin("7");

        assertThat(target).startsWith(QA_URI + "?");
        assertThat(target).contains("access_token=access", "refresh_token=refresh");
        assertThat(target).contains("invite=1", "room=7");
    }

    @Test
    void 초대가_아니면_방_번호를_붙이지_않는다() throws Exception {
        String target = redirectUrlAfterLogin(null);

        assertThat(target).contains("access_token=access");
        assertThat(target).doesNotContain("invite=", "room=");
    }

    @Test
    void 숫자가_아닌_방_번호는_무시한다() {
        assertThat(cycle(QA_URI, "7; DROP TABLE").inviteRoomId()).isNull();
        assertThat(cycle(QA_URI, "0").inviteRoomId()).isNull();
        assertThat(cycle(QA_URI, "-3").inviteRoomId()).isNull();
    }

    @Test
    void 같은_state_를_두_번_쓰면_두_번째는_빈손이다() {
        start(QA_URI, "42");

        assertThat(callback()).isNotNull();
        // 한 번 쓰면 지워지므로 인가 코드를 재사용해도 통과하지 못한다
        assertThat(callback()).isNull();
        assertThat(redis).isEmpty();
    }

    @Test
    void 모르는_state_로_들어오면_아무것도_돌려주지_않는다() {
        start(QA_URI, "42");

        MockHttpServletRequest callback = new MockHttpServletRequest();
        callback.setParameter("state", "다른-state");

        assertThat(repository.removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
        assertThat(repository.loadAuthorizationRequest(callback)).isNull();
    }

    /** 로그인 시작 → 콜백을 한 바퀴 돌리고 콜백에서 복원된 문맥을 돌려준다. */
    private OAuth2AuthorizationSession cycle(String requestedRedirectUri, String roomId) {
        start(requestedRedirectUri, roomId);
        return callback();
    }

    private void start(String requestedRedirectUri, String roomId) {
        MockHttpServletRequest start = new MockHttpServletRequest();
        start.setRequestURI("/oauth2/authorization/google");
        if (requestedRedirectUri != null) {
            start.setParameter(RedisOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM, requestedRedirectUri);
        }
        if (roomId != null) {
            start.setParameter(RedisOAuth2AuthorizationRequestRepository.ROOM_ID_PARAM, roomId);
        }
        repository.saveAuthorizationRequest(authorizationRequest(), start, new MockHttpServletResponse());
    }

    private OAuth2AuthorizationSession callback() {
        MockHttpServletRequest callback = new MockHttpServletRequest();
        callback.setParameter("state", STATE);
        repository.removeAuthorizationRequest(callback, new MockHttpServletResponse());
        return (OAuth2AuthorizationSession)
                callback.getAttribute(RedisOAuth2AuthorizationRequestRepository.SESSION_ATTRIBUTE);
    }

    /** 콜백 요청을 그대로 SuccessHandler 에 태워 프론트로 나가는 최종 주소를 얻는다. */
    private String redirectUrlAfterLogin(String roomId) throws Exception {
        start(QA_URI, roomId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("state", STATE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        repository.removeAuthorizationRequest(request, response);

        handler.onAuthenticationSuccess(request, response, authentication());
        return response.getRedirectedUrl();
    }

    private OAuth2AuthorizationRequest authorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test")
                .redirectUri("https://api.example.com/login/oauth2/code/google")
                .state(STATE)
                .attributes(Map.of("registration_id", "google"))
                .build();
    }

    private Authentication authentication() {
        CustomOAuth2User principal = new CustomOAuth2User(User.builder().build(), Map.of());
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private RedisOAuth2AuthorizationRequestRepository repository() {
        RedisOAuth2AuthorizationRequestRepository created =
                new RedisOAuth2AuthorizationRequestRepository(inMemoryRedis());
        ReflectionTestUtils.setField(created, "ttlSeconds", 300L);
        return created;
    }

    private OAuth2AuthenticationSuccessHandler handler() {
        AuthService authService = mock(AuthService.class);
        when(authService.issueTokens(any())).thenReturn(TokenResponse.of("access", "refresh", 3600L));

        OAuth2AuthenticationSuccessHandler created = new OAuth2AuthenticationSuccessHandler(authService);
        ReflectionTestUtils.setField(created, "redirectUri", DEFAULT_URI);
        ReflectionTestUtils.setField(created, "allowedRedirectUris", List.of(DEFAULT_URI, QA_URI));
        return created;
    }

    /** Redis 없이 돌리기 위한 맵 기반 대역. 키 만료는 검증 대상이 아니라 TTL 은 받기만 한다. */
    @SuppressWarnings("unchecked")
    private StringRedisTemplate inMemoryRedis() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = mock(ValueOperations.class);

        when(template.opsForValue()).thenReturn(operations);
        doAnswer(invocation -> redis.put(invocation.getArgument(0), invocation.getArgument(1)))
                .when(operations).set(anyString(), anyString(), any(Duration.class));
        when(operations.get(anyString())).thenAnswer(invocation -> redis.get(invocation.getArgument(0)));
        when(template.delete(anyString())).thenAnswer(invocation -> redis.remove(invocation.getArgument(0)) != null);

        return template;
    }
}
