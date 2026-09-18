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
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void 미션_클리어_알림은_제출자를_뺀_활성_팀원에게_저장된다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        NotificationService service = new NotificationService(notificationRepository, teamMemberRepository);

        Team team = team(10L);
        User submitter = user(1L, "민지");
        User teammate = user(2L, "준호");
        when(teamMemberRepository.findAllByTeamIdAndIsActiveTrue(team.getId()))
                .thenReturn(List.of(member(team, submitter), member(team, teammate)));

        service.createMissionClearNotifications(team, submitter, 300);

        assertThat(savedNotifications(notificationRepository))
                .singleElement()
                .satisfies(notification -> {
                    // 본인이 방금 한 일을 다시 알려줄 필요가 없다
                    assertThat(notification.getUser().getId()).isEqualTo(teammate.getId());
                    assertThat(notification.getType()).isEqualTo(NotificationType.MISSION_CLEAR);
                    assertThat(notification.getTitle()).isEqualTo("미션 클리어");
                    assertThat(notification.getBody())
                            .isEqualTo("민지님이 미션을 클리어했습니다. 300포인트를 획득했습니다.");
                });
    }

    @Test
    void 혼자_하는_방이면_알림이_생기지_않는다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        NotificationService service = new NotificationService(notificationRepository, teamMemberRepository);

        Team team = team(10L);
        User submitter = user(1L, "민지");
        when(teamMemberRepository.findAllByTeamIdAndIsActiveTrue(team.getId()))
                .thenReturn(List.of(member(team, submitter)));

        service.createMissionClearNotifications(team, submitter, 300);

        assertThat(savedNotifications(notificationRepository)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static List<Notification> savedNotifications(NotificationRepository repository) {
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void 알림_목록과_읽지_않은_개수를_조회한다() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationService service = new NotificationService(notificationRepository, mock(TeamMemberRepository.class));
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
        NotificationService service = new NotificationService(notificationRepository, mock(TeamMemberRepository.class));
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
        NotificationService service = new NotificationService(notificationRepository, mock(TeamMemberRepository.class));
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
