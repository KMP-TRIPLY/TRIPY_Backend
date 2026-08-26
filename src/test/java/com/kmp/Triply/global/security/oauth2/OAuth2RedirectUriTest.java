package com.kmp.Triply.global.security.oauth2;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 로그인 시작 시 요청한 콜백을 목록과 대조해 고르는 부분만 검증한다.
 * 토큰 발급 경로는 타지 않으므로 AuthService 는 null.
 */
class OAuth2RedirectUriTest {

    private static final String DEFAULT_URI = "https://triply-six.vercel.app/oauth2/callback";
    private static final String QA_URI = "https://triply-qa.onrender.com";

    private final HttpCookieOAuth2AuthorizationRequestRepository repository =
            new HttpCookieOAuth2AuthorizationRequestRepository();

    private final OAuth2AuthenticationSuccessHandler handler = handler();

    private OAuth2AuthenticationSuccessHandler handler() {
        OAuth2AuthenticationSuccessHandler created = new OAuth2AuthenticationSuccessHandler(null, repository);
        ReflectionTestUtils.setField(created, "redirectUri", DEFAULT_URI);
        ReflectionTestUtils.setField(created, "allowedRedirectUris", List.of(DEFAULT_URI, QA_URI));
        return created;
    }

    @Test
    void 목록에_있는_콜백을_요청하면_그_주소로_보낸다() {
        assertThat(resolve(QA_URI)).isEqualTo(QA_URI);
        assertThat(resolve(DEFAULT_URI)).isEqualTo(DEFAULT_URI);
    }

    @Test
    void 요청이_없으면_기본_콜백을_쓴다() {
        assertThat(resolve(null)).isEqualTo(DEFAULT_URI);
        assertThat(resolve("   ")).isEqualTo(DEFAULT_URI);
    }

    @Test
    void 목록에_없는_주소는_무시하고_기본_콜백을_쓴다() {
        assertThat(resolve("https://evil.example.com/steal")).isEqualTo(DEFAULT_URI);
        // 부분 일치로 뚫리지 않아야 한다
        assertThat(resolve(QA_URI + ".evil.com")).isEqualTo(DEFAULT_URI);
        assertThat(resolve("https://triply-qa.onrender.com.evil.com")).isEqualTo(DEFAULT_URI);
    }

    /** 로그인 시작(쿠키 저장) → 콜백(쿠키 사용) 한 바퀴. */
    private String resolve(String requestedRedirectUri) {
        MockHttpServletRequest start = new MockHttpServletRequest();
        if (requestedRedirectUri != null) {
            start.setParameter("redirect_uri", requestedRedirectUri);
        }
        MockHttpServletResponse startResponse = new MockHttpServletResponse();
        repository.saveAuthorizationRequest(
                org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.authorizationCode()
                        .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                        .clientId("test")
                        .redirectUri("https://api.example.com/login/oauth2/code/kakao")
                        .state("state")
                        .build(),
                start, startResponse);

        List<Cookie> cookies = new ArrayList<>();
        for (String header : startResponse.getHeaders("Set-Cookie")) {
            String[] pair = header.split(";", 2)[0].split("=", 2);
            if (pair.length == 2 && !pair[1].isEmpty()) {
                cookies.add(new Cookie(pair[0], pair[1]));
            }
        }
        MockHttpServletRequest callback = new MockHttpServletRequest();
        callback.setCookies(cookies.toArray(new Cookie[0]));
        return handler.resolveRedirectUri(callback, new MockHttpServletResponse());
    }
}
