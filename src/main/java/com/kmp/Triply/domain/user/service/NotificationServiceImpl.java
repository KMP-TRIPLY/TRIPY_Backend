package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.user.dto.response.NotificationReadAllResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationUnreadCountResponse;
import com.kmp.Triply.domain.user.entity.Notification;
import com.kmp.Triply.domain.user.entity.NotificationType;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.NotificationRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final String MISSION_CLEAR_TITLE = "미션 클리어";

    private final NotificationRepository notificationRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public NotificationUnreadCountResponse getUnreadCount(Long userId) {
        return NotificationUnreadCountResponse.of(
                notificationRepository.countByUserIdAndIsReadFalse(userId)
        );
    }

    @Override
    @Transactional
    public NotificationResponse readNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public NotificationReadAllResponse readAllNotifications(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        unreadNotifications.forEach(Notification::read);
        return NotificationReadAllResponse.of(unreadNotifications.size());
    }

    @Override
    @Transactional
    public void createMissionClearNotifications(Team team, User submitter, int scoreEarned) {
        String body = submitter.getNickname() + "님이 미션을 클리어했습니다. "
                + scoreEarned + "포인트를 획득했습니다.";

        List<Notification> notifications = teamMemberRepository.findAllByTeamIdAndIsActiveTrue(team.getId()).stream()
                .map(TeamMember::getUser)
                .map(user -> Notification.builder()
                        .user(user)
                        .type(NotificationType.MISSION_CLEAR)
                        .title(MISSION_CLEAR_TITLE)
                        .body(body)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
