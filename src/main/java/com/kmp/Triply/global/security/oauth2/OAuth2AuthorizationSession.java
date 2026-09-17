package com.kmp.Triply.global.security.oauth2;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * 로그인을 시작한 시점의 문맥. state 하나로 묶어 서버에 보관한다.
 *
 * <p>카카오톡 인앱 브라우저에서 구글 로그인을 누르면 구글이 WebView 를 막아 외부 브라우저로
 * 넘어간다. 쿠키도 localStorage 도 브라우저별이라 콜백을 받는 브라우저에는 아무것도 없다.
 * 그래서 인가 요청과 초대 정보를 전부 여기에 담아 state 를 열쇠로 되찾는다.
 *
 * @param authorizationRequest  Spring Security 가 만든 인가 요청. state 검증에 그대로 쓰인다
 * @param registrationId        로그인 제공자 (kakao/naver/google)
 * @param requestedRedirectUri  로그인 시작 시 요청한 프론트 콜백. 허용 목록 검증은 SuccessHandler 가 한다
 * @param inviteRoomId          초대 링크로 들어온 경우의 게임방 번호. 아니면 null
 */
public record OAuth2AuthorizationSession(
        OAuth2AuthorizationRequest authorizationRequest,
        String registrationId,
        String requestedRedirectUri,
        Long inviteRoomId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
