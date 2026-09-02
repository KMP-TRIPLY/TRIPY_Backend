package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MissionSubmitRequest {

    @NotNull
    private Long roomId;

    // QUIZ_TEXT / QUIZ_CHOICE 제출값
    private String submittedValue;

    // PHOTO / NFC / AR / VOICE 인증 제출물
    private String photoUrl;
}