package com.kmp.Triply.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmp.Triply.domain.user.entity.Notification;
import com.kmp.Triply.domain.user.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String body;
    @JsonProperty("isRead")
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
