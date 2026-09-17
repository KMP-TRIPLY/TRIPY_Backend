package com.kmp.Triply.global.security.oauth2;

import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.security.oauth2.userinfo.OAuth2UserInfo;
import com.kmp.Triply.global.security.oauth2.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /** 탈퇴 후 다시 가입할 수 있을 때까지 기다리는 날수. */
    @Value("${app.withdrawal.rejoin-wait-days}")
    private long rejoinWaitDays;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        SocialProvider provider = SocialProvider.valueOf(registrationId);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(provider, oAuth2User.getAttributes());

        return new CustomOAuth2User(resolveUser(userInfo, provider), oAuth2User.getAttributes());
    }

    /** 소셜 정보로 로그인할 계정을 정한다. 소셜 서버 호출과 분리해 두어 그대로 검증할 수 있다. */
    User resolveUser(OAuth2UserInfo userInfo, SocialProvider provider) {
        Optional<User> found = userRepository.findBySocialProviderAndSocialId(provider, userInfo.getSocialId());
        if (found.isPresent() && found.get().isDeleted()) {
            // 대기 기간이 남았으면 여기서 끊긴다. 지났으면 옛 계정을 비켜 두고 새로 가입시킨다.
            detachWithdrawnAccount(found.get());
            found = Optional.empty();
        }

        User user = found.orElseGet(() -> registerNewUser(userInfo, provider));

        // 프로필 이미지가 비어 있으면 소셜 프로필 이미지로 채움 (기존 null 계정 로그인 시 복구)
        if (user.getProfileImg() == null && userInfo.getProfileImg() != null) {
            user.updateProfile(null, userInfo.getProfileImg());
        }
        return user;
    }

    /**
     * 탈퇴한 계정을 비켜 준다. 대기 기간이 남았으면 로그인을 막고,
     * 다 지났으면 소셜 연결을 떼어내 같은 소셜 계정으로 새로 가입할 수 있게 한다.
     *
     * <p>예전 계정의 여행 코스·리워드·기록은 따라오지 않는다. 탈퇴는 되돌리는 것이 아니라
     * 끊는 것이고, 지웠다고 알린 데이터를 되살리지 않기로 했다.
     */
    private void detachWithdrawnAccount(User withdrawn) {
        long daysLeft = rejoinWaitDaysLeft(withdrawn);
        if (daysLeft > 0) {
            throw new WithdrawnUserException(daysLeft);
        }
        withdrawn.releaseSocialIdentity();
        // 새 행을 넣기 전에 유니크 자리를 먼저 비워야 한다. 같은 트랜잭션이라
        // flush 하지 않으면 INSERT 가 먼저 나가 social_id 유니크 제약에 걸린다.
        userRepository.saveAndFlush(withdrawn);
    }

    private long rejoinWaitDaysLeft(User withdrawn) {
        long daysPassed = ChronoUnit.DAYS.between(withdrawn.getDeletedAt(), LocalDateTime.now());
        return Math.max(0, rejoinWaitDays - daysPassed);
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
        return userRepository.save(user);
    }
}