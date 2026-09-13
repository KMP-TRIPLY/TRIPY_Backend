package com.kmp.Triply.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationUnreadCountResponse {

    private long unreadCount;

    public static NotificationUnreadCountResponse of(long unreadCount) {
        return NotificationUnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }
}
