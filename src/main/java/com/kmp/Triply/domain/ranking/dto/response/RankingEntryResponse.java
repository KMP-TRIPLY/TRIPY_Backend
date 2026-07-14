package com.kmp.Triply.domain.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingEntryResponse {

    private int rank;
    private Long targetId;
    private String targetName;
    private int score;
    private Integer elapsedSeconds;
    private Short missionClearCount;
    private Short hintUsedCount;

    public static RankingEntryResponse of(int rank, Long targetId, String targetName, int score,
                                          Integer elapsedSeconds, Short missionClearCount, Short hintUsedCount) {
        return RankingEntryResponse.builder()
                .rank(rank)
                .targetId(targetId)
                .targetName(targetName)
                .score(score)
                .elapsedSeconds(elapsedSeconds)
                .missionClearCount(missionClearCount)
                .hintUsedCount(hintUsedCount)
                .build();
    }
}
