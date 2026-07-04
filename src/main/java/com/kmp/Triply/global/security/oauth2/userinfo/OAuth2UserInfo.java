package com.kmp.Triply.global.security.oauth2.userinfo;

public interface OAuth2UserInfo {
    String getSocialId();
    String getEmail();
    String getNickname();
    String getProfileImg();
}
