package com.kmp.Triply.domain.ranking.service;

import com.kmp.Triply.domain.ranking.dto.request.RankingMode;
import com.kmp.Triply.domain.ranking.dto.response.RankingResponse;

public interface RankingService {

    RankingResponse getLiveRankings(Long gameRoomId);

    RankingResponse getCourseRankings(Long courseId, RankingMode mode);
}
