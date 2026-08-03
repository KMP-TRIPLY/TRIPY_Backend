package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameProgress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpotArriveResponse {

    private Long spotId;
    private String status;
    private Integer distanceMeters;
    private int missionCount;

    public static SpotArriveResponse of(GameProgress progress, Integer distanceMeters, int missionCount) {
        return SpotArriveResponse.builder()
                .spotId(progress.getCourseSpot().getId())
                .status(progress.getStatus().name())
                .distanceMeters(distanceMeters)
                .missionCount(missionCount)
                .build();
    }
}