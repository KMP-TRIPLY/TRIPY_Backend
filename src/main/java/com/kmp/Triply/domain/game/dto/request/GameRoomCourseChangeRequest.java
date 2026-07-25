package com.kmp.Triply.domain.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "게임방 코스 변경 요청")
public class GameRoomCourseChangeRequest {

    @NotNull
    @Schema(description = "변경할 코스 ID", example = "2")
    private Long courseId;
}
