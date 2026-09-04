package com.kmp.Triply.domain.game.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GameRoomCreateRequest {

    @NotNull
    private Long courseId;

    @NotBlank
    @Size(max = 50)
    private String roomName;

    /**
     * 방 비밀번호. 모든 방이 잠긴 방이라 필수다.
     *
     * <p>여럿이 입으로 공유하는 임시 비번이라 숫자 5자리로 고정한다 - 불러주기 쉽고
     * 앱이 숫자 키패드를 띄울 수 있다. 계정 비번처럼 대소문자·특수문자를 요구하면
     * 공유가 번거로워지기만 한다.
     */
    @NotBlank(message = "방 비밀번호는 필수입니다.")
    @Pattern(regexp = "\\d{5}", message = "방 비밀번호는 숫자 5자리입니다.")
    private String password;

    /** 방 정원. 1 이면 혼자 하는 방(SOLO)이 되어 다른 사람이 들어올 수 없다. */
    @Min(1)
    @Max(10)
    private short maxMembers = 4;
}
