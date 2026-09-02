package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GameRoomCreateRequest {

    @NotNull
    private Long courseId;

    @NotBlank
    @Size(max = 50)
    private String roomName;

    /** 방 정원. 1 이면 혼자 하는 방(SOLO)이 되어 다른 사람이 들어올 수 없다. */
    @Min(1)
    @Max(10)
    private short maxMembers = 4;
}
