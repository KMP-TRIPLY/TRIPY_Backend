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
    private String teamName;

    @Min(2)
    @Max(10)
    private short maxTeams = 4;
}
