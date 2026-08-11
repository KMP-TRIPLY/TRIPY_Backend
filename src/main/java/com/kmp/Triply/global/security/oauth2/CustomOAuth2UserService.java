package com.kmp.Triply.global.security.oauth2;

import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.security.oauth2.userinfo.OAuth2UserInfo;
import com.kmp.Triply.global.security.oauth2.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // @Transactional 을 걸지 않는다. 소셜 서버 HTTP 호출(super.loadUser) 동안 DB 트랜잭션을 잡고 있게 되고,
    // 중복 INSERT 가 터지면 트랜잭션이 롤백 전용으로 물들어 뒤이은 재조회까지 실패한다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        SocialProvider provider = SocialProvider.valueOf(registrationId);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(provider, oAuth2User.getAttributes());

        User user = userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(provider, userInfo.getSocialId())
                .orElseGet(() -> registerNewUser(userInfo, provider));

        // 프로필 이미지가 비어 있으면 소셜 프로필 이미지로 채움 (기존 null 계정 로그인 시 복구)
        if (user.getProfileImg() == null && userInfo.getProfileImg() != null) {
            user.updateProfile(null, userInfo.getProfileImg());
            userRepository.save(user);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo userInfo, SocialProvider provider) {
        String email = userInfo.getEmail() != null
                ? userInfo.getEmail()
                : provider.name().toLowerCase() + "_" + userInfo.getSocialId() + "@triply.app";
        User user = User.builder()
                .email(email)
                .nickname(userInfo.getNickname() != null ? userInfo.getNickname() : "여행자")
                .profileImg(userInfo.getProfileImg())
                .socialProvider(provider)
                .socialId(userInfo.getSocialId())
                .build();
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // 같은 계정의 첫 로그인이 동시에 들어온 경우. 먼저 저장된 쪽을 쓴다.
            return userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(provider, userInfo.getSocialId())
                    .orElseThrow(() -> e);
        }
    }
}