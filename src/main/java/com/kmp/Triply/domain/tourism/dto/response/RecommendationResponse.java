package com.kmp.Triply.domain.tourism.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private String contentId;       // hubTatsCd
    private String title;           // hubTatsNm
    private String mapX;
    private String mapY;
    private String areaCd;
    private String areaNm;
    private String signguCd;
    private String signguNm;
    private String categoryLarge;   // hubCtgryLclsNm
    private String categoryMiddle;  // hubCtgryMclsNm
    private int rank;               // hubRank
}