package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.AttemptResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionSubmitResponse {

    private Long missionId;
    private AttemptResult result;
    private boolean correct;
    private int scoreEarned;
    private boolean hintUsed;
    private int teamTotalScore;
    private boolean spotCompleted;
    private Long nextSpotId;
}