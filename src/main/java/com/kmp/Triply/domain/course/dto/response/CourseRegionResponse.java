package com.kmp.Triply.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "코스가 존재하는 지역")
public record CourseRegionResponse(
        @Schema(description = "국가행정표준 시도 코드", example = "44") String regionCode,
        @Schema(description = "시도 이름", example = "충청남도") String regionName) {

    // 국가행정표준코드 시도 코드. 관광공사 API(areaCd)와 동일 기준.
    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("11", "서울특별시"),
            Map.entry("26", "부산광역시"),
            Map.entry("27", "대구광역시"),
            Map.entry("28", "인천광역시"),
            Map.entry("29", "광주광역시"),
            Map.entry("30", "대전광역시"),
            Map.entry("31", "울산광역시"),
            Map.entry("36", "세종특별자치시"),
            Map.entry("41", "경기도"),
            Map.entry("43", "충청북도"),
            Map.entry("44", "충청남도"),
            Map.entry("46", "전라남도"),
            Map.entry("47", "경상북도"),
            Map.entry("48", "경상남도"),
            Map.entry("50", "제주특별자치도"),
            Map.entry("51", "강원특별자치도"),
            Map.entry("52", "전북특별자치도"));

    public static CourseRegionResponse from(String regionCode) {
        return new CourseRegionResponse(regionCode, NAMES.getOrDefault(regionCode, regionCode));
    }
}
