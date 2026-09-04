package com.kmp.Triply.domain.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "게임방 참여 요청. 잠기지 않은 방이면 본문 없이 보내도 된다")
public class GameRoomJoinRequest {

    @Schema(description = "방 비밀번호(숫자 5자리). 잠긴 방(locked=true)에만 필요하다", example = "12345")
    @Pattern(regexp = "^$|^\\d{5}$", message = "방 비밀번호는 숫자 5자리입니다.")
    private String password;
}
