package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GameRoomJoinRequest {

    /** 방 코드만으로 참여한다. 비밀번호는 쓰지 않는다. */
    @NotBlank
    @Size(max = 8)
    private String roomCode;
}
