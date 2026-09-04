package com.kmp.Triply.domain.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "게임방 참여 요청")
public class GameRoomJoinRequest {

    @Schema(description = "방 비밀번호(숫자 5자리)", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "방 비밀번호는 필수입니다.")
    @Pattern(regexp = "\\d{5}", message = "방 비밀번호는 숫자 5자리입니다.")
    private String password;
}
