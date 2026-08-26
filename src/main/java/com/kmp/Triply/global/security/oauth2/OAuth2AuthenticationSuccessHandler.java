package com.kmp.Triply.global.security.oauth2;

import com.kmp.Triply.domain.user.dto.response.TokenResponse;
import com.kmp.Triply.domain.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    /** 토큰을 실어 보낼 수 있는 콜백 목록. 여기 없는 주소로는 절대 보내지 않는다 (토큰 탈취 방지). */
    @Value("${app.oauth2.allowed-redirect-uris}")
    private List<String> allowedRedirectUris;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        TokenResponse tokens = authService.issueTokens(oAuth2User.getUser());

        String targetUrl = UriComponentsBuilder.fromUriString(resolveRedirectUri(request, response))
                .queryParam("access_token", tokens.getAccessToken())
                .queryParam("refresh_token", tokens.getRefreshToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 로그인 시작 시 ?redirect_uri= 로 요청한 주소를 쓴다.
     * 목록에 없거나 없으면 기본 콜백. 부분 일치는 열린 리다이렉트가 되므로 완전 일치만 인정한다.
     */
    String resolveRedirectUri(HttpServletRequest request, HttpServletResponse response) {
        return authorizationRequestRepository.popRequestedRedirectUri(request, response)
                .filter(allowedRedirectUris::contains)
                .orElse(redirectUri);
    }
}