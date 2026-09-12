package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.user.dto.response.NotificationReadAllResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationResponse;
import com.kmp.Triply.domain.user.dto.response.NotificationUnreadCountResponse;
import com.kmp.Triply.domain.user.entity.User;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Long userId);

    NotificationUnreadCountResponse getUnreadCount(Long userId);

    NotificationResponse readNotification(Long userId, Long notificationId);

    NotificationReadAllResponse readAllNotifications(Long userId);

    void createMissionClearNotifications(Team team, User submitter, int scoreEarned);
}
