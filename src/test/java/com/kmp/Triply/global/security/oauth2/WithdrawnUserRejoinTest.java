package com.kmp.Triply.global.security.oauth2;

import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.security.oauth2.userinfo.OAuth2UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 탈퇴 후 재가입 규칙. 기다리는 동안은 로그인 자체를 끊고,
 * 기간이 지나면 예전 데이터 없이 새 계정으로 시작한다.
 */
class WithdrawnUserRejoinTest {

    private static final SocialProvider PROVIDER = SocialProvider.GOOGLE;
    private static final String SOCIAL_ID = "google-123";
    private static final long WAIT_DAYS = 7;

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomOAuth2UserService service = service();

    @Test
    void 탈퇴한_지_얼마_안_됐으면_로그인을_막고_남은_날수를_알려준다() {
        givenExistingUser(withdrawnDaysAgo(2));

        assertThatThrownBy(() -> service.resolveUser(userInfo(), PROVIDER))
                .isInstanceOf(WithdrawnUserException.class)
                .extracting(e -> ((WithdrawnUserException) e).getRetryAfterDays())
                .isEqualTo(5L);

        // 막혔으므로 새 계정도 만들지 않는다
        verify(userRepository, never()).save(any());
    }

    @Test
    void 대기_기간이_꽉_차기_전날까지는_막힌다() {
        givenExistingUser(withdrawnDaysAgo(6));

        assertThatThrownBy(() -> service.resolveUser(userInfo(), PROVIDER))
                .isInstanceOf(WithdrawnUserException.class)
                .extracting(e -> ((WithdrawnUserException) e).getRetryAfterDays())
                .isEqualTo(1L);
    }

    @Test
    void 대기_기간이_지나면_예전_계정을_비켜두고_새_계정으로_가입시킨다() {
        User withdrawn = withdrawnDaysAgo(7);
        givenExistingUser(withdrawn);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User rejoined = service.resolveUser(userInfo(), PROVIDER);

        // 옛 계정은 유니크 자리를 비우고 익명 껍데기로 남는다 (게임 기록이 user_id 를 참조한다)
        assertThat(withdrawn.getSocialId()).isEqualTo("withdrawn_10");
        assertThat(withdrawn.getEmail()).isEqualTo("withdrawn_10@triply.invalid");
        assertThat(withdrawn.getNickname()).isEqualTo("탈퇴한 사용자");
        assertThat(withdrawn.isDeleted()).isTrue();
        verify(userRepository).saveAndFlush(withdrawn);

        // 새 계정은 예전 것을 물려받지 않는다
        assertThat(rejoined).isNotSameAs(withdrawn);
        assertThat(rejoined.getSocialId()).isEqualTo(SOCIAL_ID);
        assertThat(rejoined.getNickname()).isEqualTo("민지");
        assertThat(rejoined.getLevel()).isEqualTo(1);
        assertThat(rejoined.isDeleted()).isFalse();
    }

    @Test
    void 탈퇴하지_않은_계정은_그대로_로그인된다() {
        User active = user(20L, "준호");
        givenExistingUser(active);

        assertThat(service.resolveUser(userInfo(), PROVIDER)).isSameAs(active);
        verify(userRepository, never()).saveAndFlush(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 처음_보는_소셜_계정은_새로_가입시킨다() {
        when(userRepository.findBySocialProviderAndSocialId(PROVIDER, SOCIAL_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.resolveUser(userInfo(), PROVIDER).getSocialId()).isEqualTo(SOCIAL_ID);
    }

    @Test
    void 막힌_로그인은_사유와_남은_날수를_콜백으로_돌려준다() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "redirectUri", "https://triply-six.vercel.app/oauth2/callback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(new MockHttpServletRequest(), response, new WithdrawnUserException(5));

        assertThat(response.getRedirectedUrl())
                .startsWith("https://triply-six.vercel.app/oauth2/callback?")
                .contains("error=WITHDRAWN_USER")
                .contains("retry_after_days=5");
    }

    private CustomOAuth2UserService service() {
        CustomOAuth2UserService created = new CustomOAuth2UserService(userRepository);
        ReflectionTestUtils.setField(created, "rejoinWaitDays", WAIT_DAYS);
        return created;
    }

    private void givenExistingUser(User user) {
        when(userRepository.findBySocialProviderAndSocialId(PROVIDER, SOCIAL_ID)).thenReturn(Optional.of(user));
    }

    private User withdrawnDaysAgo(int days) {
        User user = user(10L, "민지");
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now().minusDays(days));
        return user;
    }

    private User user(Long id, String nickname) {
        User user = User.builder()
                .email(id + "@triply.test")
                .nickname(nickname)
                .profileImg("https://img.example/" + id + ".png")
                .socialProvider(PROVIDER)
                .socialId(SOCIAL_ID)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private OAuth2UserInfo userInfo() {
        return new OAuth2UserInfo() {
            @Override public String getSocialId() { return SOCIAL_ID; }
            @Override public String getEmail() { return "minji@triply.test"; }
            @Override public String getNickname() { return "민지"; }
            @Override public String getProfileImg() { return "https://img.example/new.png"; }
        };
    }
}
