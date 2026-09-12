package com.kmp.Triply.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadAllResponse {

    private int readCount;

    public static NotificationReadAllResponse of(int readCount) {
        return NotificationReadAllResponse.builder()
                .readCount(readCount)
                .build();
    }
}
