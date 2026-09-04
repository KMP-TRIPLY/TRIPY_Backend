package com.kmp.Triply.domain.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
@Schema(description = "게임방 정원 변경 요청")
public class GameRoomMaxMembersChangeRequest {

    @Schema(description = "바꿀 정원. 지금 들어와 있는 인원보다 작게는 줄일 수 없다", example = "6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    @Max(10)
    private short maxMembers;
}
