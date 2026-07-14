package com.kmp.Triply.domain.ranking.dto.response;

import com.kmp.Triply.domain.ranking.dto.request.RankingMode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RankingResponse {

    private RankingMode mode;
    private Long gameRoomId;
    private Long courseId;
    private String courseTitle;
    private List<RankingEntryResponse> rankings;

    public static RankingResponse of(RankingMode mode, Long gameRoomId, Long courseId,
                                     String courseTitle, List<RankingEntryResponse> rankings) {
        return RankingResponse.builder()
                .mode(mode)
                .gameRoomId(gameRoomId)
                .courseId(courseId)
                .courseTitle(courseTitle)
                .rankings(rankings)
                .build();
    }
}
