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
public class NotificationService {

    private static final String MISSION_CLEAR_TITLE = "미션 클리어";

    private final NotificationRepository notificationRepository;
    private final TeamMemberRepository teamMemberRepository;

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public NotificationUnreadCountResponse getUnreadCount(Long userId) {
        return NotificationUnreadCountResponse.of(
                notificationRepository.countByUserIdAndIsReadFalse(userId)
        );
    }

    @Transactional
    public NotificationResponse readNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public NotificationReadAllResponse readAllNotifications(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        unreadNotifications.forEach(Notification::read);
        return NotificationReadAllResponse.of(unreadNotifications.size());
    }

    @Transactional
    public void createMissionClearNotifications(Team team, User submitter, int scoreEarned) {
        String body = submitter.getNickname() + "님이 미션을 클리어했습니다. "
                + scoreEarned + "포인트를 획득했습니다.";

        // 제출자 본인에게는 남기지 않는다. 방금 자기가 한 일을 "○○님이 클리어했습니다" 로
        // 다시 받으면 읽지 않은 알림만 늘어난다. 혼자 하는 방이면 아무에게도 안 간다.
        List<Notification> notifications = teamMemberRepository.findAllByTeamIdAndIsActiveTrue(team.getId()).stream()
                .map(TeamMember::getUser)
                .filter(user -> !user.getId().equals(submitter.getId()))
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
