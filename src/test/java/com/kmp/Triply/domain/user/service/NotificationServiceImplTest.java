package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.user.dto.response.NotificationReadAllResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationUnreadCountResponse;
import com.kmp.Triply.domain.user.entity.Notification;
import com.kmp.Triply.domain.user.entity.NotificationType;
import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    @Test
    void 미션_클리어_알림은_활성_팀원_전체에게_저장된다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, teamMemberRepository);

        Team team = team(10L);
        User submitter = user(1L, "민지");
        User teammate = user(2L, "준호");
        when(teamMemberRepository.findAllByTeamIdAndIsActiveTrue(team.getId()))
                .thenReturn(List.of(member(team, submitter), member(team, teammate)));

        service.createMissionClearNotifications(team, submitter, 300);

        verify(notificationRepository).saveAll(anyList());
        verify(notificationRepository).saveAll(org.mockito.ArgumentMatchers.argThat(notifications -> {
            List<Notification> saved = (List<Notification>) notifications;
            return saved.size() == 2
                    && saved.stream().allMatch(notification -> notification.getType() == NotificationType.MISSION_CLEAR)
                    && saved.stream().allMatch(notification -> notification.getTitle().equals("미션 클리어"))
                    && saved.stream().allMatch(notification -> notification.getBody()
                            .equals("민지님이 미션을 클리어했습니다. 300포인트를 획득했습니다."));
        }));
    }

    @Test
    void 알림_목록과_읽지_않은_개수를_조회한다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, mock(TeamMemberRepository.class));
        User user = user(1L, "민지");
        Notification notification = notification(user);
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(user.getId()))
                .thenReturn(List.of(notification));
        when(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).thenReturn(3L);

        List<NotificationResponse> notifications = service.getNotifications(user.getId());
        NotificationUnreadCountResponse unreadCount = service.getUnreadCount(user.getId());

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle()).isEqualTo("미션 클리어");
        assertThat(unreadCount.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    void 본인_알림을_읽음_처리한다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, mock(TeamMemberRepository.class));
        User user = user(1L, "민지");
        Notification notification = notification(user);
        when(notificationRepository.findByIdAndUserId(100L, user.getId()))
                .thenReturn(Optional.of(notification));

        NotificationResponse response = service.readNotification(user.getId(), 100L);

        assertThat(response.isRead()).isTrue();
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void 읽지_않은_알림을_모두_읽음_처리한다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, mock(TeamMemberRepository.class));
        User user = user(1L, "민지");
        Notification first = notification(user);
        Notification second = notification(user);
        when(notificationRepository.findAllByUserIdAndIsReadFalse(user.getId()))
                .thenReturn(List.of(first, second));

        NotificationReadAllResponse response = service.readAllNotifications(user.getId());

        assertThat(response.getReadCount()).isEqualTo(2);
        assertThat(first.isRead()).isTrue();
        assertThat(second.isRead()).isTrue();
    }

    private static Notification notification(User user) {
        return Notification.builder()
                .user(user)
                .type(NotificationType.MISSION_CLEAR)
                .title("미션 클리어")
                .body("민지님이 미션을 클리어했습니다. 300포인트를 획득했습니다.")
                .build();
    }

    private static Team team(Long id) {
        Team team = Team.builder()
                .teamName("공주 원정대")
                .build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private static TeamMember member(Team team, User user) {
        return TeamMember.builder()
                .team(team)
                .user(user)
                .build();
    }

    private static User user(Long id, String nickname) {
        User user = User.builder()
                .email(id + "@triply.test")
                .nickname(nickname)
                .socialProvider(SocialProvider.GOOGLE)
                .socialId("social-" + id)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
