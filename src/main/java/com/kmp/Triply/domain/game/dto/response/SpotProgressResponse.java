package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.ProgressStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpotProgressResponse {

    private Long spotId;
    private short sequenceOrder;
    private ProgressStatus status;
    private int totalMissions;
    private int solvedMissions;
}
