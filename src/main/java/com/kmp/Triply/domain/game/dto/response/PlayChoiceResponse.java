package com.kmp.Triply.domain.game.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 게임 진행 중 노출되는 선택지. 정답 여부(is_correct)는 절대 포함하지 않는다.
 */
@Getter
@Builder
public class PlayChoiceResponse {

    private String label;
    private String value;
}