package com.kmp.Triply.global.security.oauth2.userinfo;

import com.kmp.Triply.domain.user.entity.SocialProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo of(SocialProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
        };
    }
}