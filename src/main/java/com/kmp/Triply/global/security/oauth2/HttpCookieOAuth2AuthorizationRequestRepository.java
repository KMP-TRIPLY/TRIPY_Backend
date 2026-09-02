package com.kmp.Triply.global.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Optional;

/**
 * OAuth2 인증 요청을 서버 세션 대신 쿠키에 저장하는 저장소.
 * Render 등 인스턴스가 재시작/슬립되는 환경에서 세션이 유실되어
 * authorization_request_not_found 가 발생하는 문제를 방지한다.
 * (STATELESS 세션 정책과도 일관됨)
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    /** 로그인 시작 시 클라이언트가 요청한 콜백 주소. 콜백 때는 파라미터가 없으므로 쿠키로 넘긴다. */
    public static final String REDIRECT_URI_COOKIE_NAME = "oauth2_redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, AUTH_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeCookie(response);
            return;
        }
        String value = Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(authorizationRequest));
        addCookie(response, AUTH_REQUEST_COOKIE_NAME, value, COOKIE_EXPIRE_SECONDS);

        String requestedRedirectUri = request.getParameter("redirect_uri");
        if (StringUtils.hasText(requestedRedirectUri)) {
            addCookie(response, REDIRECT_URI_COOKIE_NAME, requestedRedirectUri, COOKIE_EXPIRE_SECONDS);
        }
    }

    /** 콜백에서 클라이언트가 요청한 주소를 꺼내고 쿠키를 지운다. 검증은 SuccessHandler 가 한다. */
    public Optional<String> popRequestedRedirectUri(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> requested = getCookie(request, REDIRECT_URI_COOKIE_NAME).map(Cookie::getValue);
        addCookie(response, REDIRECT_URI_COOKIE_NAME, "", 0);
        return requested.filter(StringUtils::hasText);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeCookie(response);
        return authorizationRequest;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void removeCookie(HttpServletResponse response) {
        addCookie(response, AUTH_REQUEST_COOKIE_NAME, "", 0);
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
