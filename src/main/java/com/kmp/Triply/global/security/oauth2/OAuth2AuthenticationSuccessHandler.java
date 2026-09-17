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

        OAuth2AuthorizationSession session = (OAuth2AuthorizationSession)
                request.getAttribute(RedisOAuth2AuthorizationRequestRepository.SESSION_ATTRIBUTE);

        UriComponentsBuilder target = UriComponentsBuilder.fromUriString(resolveRedirectUri(session))
                .queryParam("access_token", tokens.getAccessToken())
                .queryParam("refresh_token", tokens.getRefreshToken());

        // 초대 링크로 시작한 로그인이면 어느 방이었는지 되돌려준다. 인앱 브라우저에서 외부
        // 브라우저로 넘어가면 프론트의 localStorage 가 통째로 바뀌어 스스로는 알 수 없다.
        Long inviteRoomId = session == null ? null : session.inviteRoomId();
        if (inviteRoomId != null) {
            target.queryParam("invite", 1).queryParam("room", inviteRoomId);
        }

        getRedirectStrategy().sendRedirect(request, response, target.build().toUriString());
    }

    /**
     * 로그인 시작 시 ?redirect_uri= 로 요청한 주소를 쓴다.
     * 목록에 없거나 없으면 기본 콜백. 부분 일치는 열린 리다이렉트가 되므로 완전 일치만 인정한다.
     */
    String resolveRedirectUri(OAuth2AuthorizationSession session) {
        String requested = session == null ? null : session.requestedRedirectUri();
        if (requested == null) {
            return redirectUri;
        }
        return allowedRedirectUris.contains(requested) ? requested : redirectUri;
    }
}
