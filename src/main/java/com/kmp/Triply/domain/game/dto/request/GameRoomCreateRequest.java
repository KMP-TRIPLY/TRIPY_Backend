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
     * 방 비밀번호. 선택이다 — 넣으면 아는 사람만 들어올 수 있고, 비우면 목록에서 누구나 들어온다.
     *
     * <p>넣을 때는 숫자 5자리다. 여럿이 입으로 공유하는 임시 비번이라 불러주기 쉬워야 하고
     * 앱이 숫자 키패드를 띄울 수 있어야 한다. 계정 비번처럼 대소문자·특수문자를 요구하면
     * 공유가 번거로워지기만 한다.
     *
     * <p>비우는 방법은 필드를 안 보내거나, null 이거나, 빈 문자열이다 — 클라이언트가
     * 빈 입력을 어느 쪽으로 보내도 공개 방이 되게 셋 다 받는다.
     */
    @Pattern(regexp = "^$|^\\d{5}$", message = "방 비밀번호는 숫자 5자리이거나 비어 있어야 합니다.")
    private String password;

    /** 방 정원. 1 이면 혼자 하는 방(SOLO)이 되어 다른 사람이 들어올 수 없다. */
    @Min(1)
    @Max(10)
    private short maxMembers = 4;
}
