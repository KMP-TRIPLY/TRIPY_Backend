package com.kmp.Triply.domain.game.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HintResponse {

    private Long missionId;
    private String hint;
    private int hintPenalty;
}