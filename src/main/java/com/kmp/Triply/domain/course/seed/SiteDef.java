package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.tourism.entity.SpotCategory;

import java.math.BigDecimal;

/** 시드 코스가 참조하는 관광지 원본. 여러 코스가 같은 siteKey 를 공유한다. */
record SiteDef(
        String siteKey, String name, SpotCategory category, String address,
        BigDecimal lat, BigDecimal lng) {

    String contentId() {
        return "MANUAL-" + siteKey.replace('_', '-');
    }
}
