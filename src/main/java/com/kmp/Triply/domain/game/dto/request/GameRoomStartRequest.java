package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GameRoomStartRequest {

    @NotNull
    private Long roomId;
}
