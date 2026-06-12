package com.kmp.Triply.global.security.oauth2.userinfo;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> response;

    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        // Naver wraps user info in "response" field
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getSocialId() {
        return response != null ? (String) response.get("id") : null;
    }

    @Override
    public String getEmail() {
        return response != null ? (String) response.get("email") : null;
    }

    @Override
    public String getNickname() {
        return response != null ? (String) response.get("nickname") : null;
    }

    @Override
    public String getProfileImg() {
        return response != null ? (String) response.get("profile_image") : null;
    }
}