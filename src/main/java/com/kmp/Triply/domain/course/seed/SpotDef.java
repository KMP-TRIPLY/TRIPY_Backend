package com.kmp.Triply.domain.course.seed;

import java.math.BigDecimal;
import java.util.List;

/**
 * 코스에 포함되는 스팟 정의.
 * lat/lng 는 관광지 대표 좌표가 아니라 미션 인증이 열리는 지오펜스 중심이다(예: 공산성 쌍수정).
 */
record SpotDef(
        String siteKey, short sequenceOrder, String storyText,
        BigDecimal lat, BigDecimal lng, int radiusMeters,
        boolean indoor, List<MissionDef> missions) {

    /** 야외 스팟. 대부분이 여기에 해당해 기본형으로 둔다. */
    SpotDef(String siteKey, short sequenceOrder, String storyText,
            BigDecimal lat, BigDecimal lng, int radiusMeters, List<MissionDef> missions) {
        this(siteKey, sequenceOrder, storyText, lat, lng, radiusMeters, false, missions);
    }
}
