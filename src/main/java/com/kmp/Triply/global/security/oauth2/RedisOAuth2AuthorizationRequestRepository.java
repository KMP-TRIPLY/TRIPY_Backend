package com.kmp.Triply.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * OAuth2 인가 요청을 쿠키 대신 Redis 에 보관하는 저장소.
 *
 * <p>이전 구현은 인가 요청을 쿠키에 담았는데, 카카오톡 인앱 브라우저에서 시작한 로그인이
 * 구글의 WebView 차단 때문에 외부 브라우저에서 끝나면 그 브라우저에는 쿠키가 없다.
 * 콜백에서 인가 요청을 못 찾아 authorization_request_not_found 로 로그인이 통째로 깨지고,
 * 초대 방 번호도 함께 사라졌다. 브라우저가 바뀌어도 따라오는 건 state 뿐이므로
 * state 를 열쇠 삼아 서버가 문맥을 들고 있는다.
 *
 * <p>state 는 Spring Security 가 만드는 128비트 난수라 추측할 수 없다. 저장한 문맥은
 * 콜백에서 한 번 쓰고 즉시 지워 재사용을 막고, 중간에 이탈한 로그인은 TTL 로 정리된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    /**
     * 콜백에서 꺼낸 문맥을 SuccessHandler 에게 넘기는 요청 속성.
     * 꺼내는 순간 Redis 에서 지우므로 다시 조회할 수 없어 같은 요청 안에서 들고 간다.
     */
    public static final String SESSION_ATTRIBUTE = OAuth2AuthorizationSession.class.getName();

    /** 로그인 시작 시 초대 방 번호를 실어 보내는 파라미터. */
    public static final String ROOM_ID_PARAM = "room_id";
    /** 로그인 시작 시 돌아갈 프론트 콜백을 고르는 파라미터. */
    public static final String REDIRECT_URI_PARAM = "redirect_uri";

    private static final String KEY_PREFIX = "oauth2:auth-request:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.oauth2.authorization-request-ttl-seconds:300}")
    private long ttlSeconds;

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        OAuth2AuthorizationSession session = new OAuth2AuthorizationSession(
                authorizationRequest,
                authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID),
                trimToNull(request.getParameter(REDIRECT_URI_PARAM)),
                parseRoomId(request.getParameter(ROOM_ID_PARAM)));

        redisTemplate.opsForValue().set(
                key(authorizationRequest.getState()),
                serialize(session),
                Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return find(stateOf(request))
                .map(OAuth2AuthorizationSession::authorizationRequest)
                .orElse(null);
    }

    /**
     * 콜백에서 문맥을 꺼내고 지운다. 같은 state 로 두 번 들어오면 두 번째는 빈손이 되어
     * 인가 코드 재사용 공격이 막힌다.
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        String state = stateOf(request);
        Optional<OAuth2AuthorizationSession> session = find(state);
        if (session.isEmpty()) {
            return null;
        }
        redisTemplate.delete(key(state));
        request.setAttribute(SESSION_ATTRIBUTE, session.get());
        return session.get().authorizationRequest();
    }

    private Optional<OAuth2AuthorizationSession> find(String state) {
        if (!StringUtils.hasText(state)) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(state)))
                .flatMap(this::deserialize);
    }

    private String key(String state) {
        return KEY_PREFIX + state;
    }

    private String stateOf(HttpServletRequest request) {
        return request.getParameter(OAuth2ParameterNames.STATE);
    }

    /** 방 번호는 그대로 프론트 콜백 URL 에 붙으므로 숫자만 통과시킨다. */
    private Long parseRoomId(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            long roomId = Long.parseLong(raw.trim());
            return roomId > 0 ? roomId : null;
        } catch (NumberFormatException e) {
            log.warn("초대 방 번호가 숫자가 아니라 무시합니다. room_id={}", raw);
            return null;
        }
    }

    private String trimToNull(String raw) {
        return StringUtils.hasText(raw) ? raw.trim() : null;
    }

    private String serialize(OAuth2AuthorizationSession session) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(session);
            out.flush();
            return Base64.getUrlEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException e) {
            // 직렬화가 깨지면 저장을 건너뛰는 대신 터뜨린다. 조용히 넘기면 콜백에서
            // authorization_request_not_found 로 둔갑해 원인을 못 찾는다.
            throw new IllegalStateException("OAuth2 인가 요청을 직렬화하지 못했습니다.", e);
        }
    }

    private Optional<OAuth2AuthorizationSession> deserialize(String value) {
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(Base64.getUrlDecoder().decode(value)))) {
            return Optional.of((OAuth2AuthorizationSession) in.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException | IllegalArgumentException e) {
            // 배포로 클래스 모양이 바뀌면 이전 형식이 남아 있을 수 있다. 로그인을 막는 대신
            // 없는 셈 치고 다시 로그인하게 둔다.
            log.warn("보관된 OAuth2 인가 요청을 읽지 못해 무시합니다.", e);
            return Optional.empty();
        }
    }
}
