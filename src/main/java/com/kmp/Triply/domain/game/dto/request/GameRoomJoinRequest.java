package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GameRoomJoinRequest {

    @NotBlank
    @Size(max = 8)
    private String roomCode;

    private Long teamId;

    @Size(max = 50)
    private String teamName;
}
