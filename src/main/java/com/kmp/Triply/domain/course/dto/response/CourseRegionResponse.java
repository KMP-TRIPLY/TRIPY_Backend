package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.RegionCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "코스가 존재하는 지역")
public record CourseRegionResponse(
        @Schema(description = "국가행정표준 시도 코드", example = "44") String regionCode,
        @Schema(description = "시도 이름", example = "충청남도") String regionName) {

    public static CourseRegionResponse from(String regionCode) {
        return new CourseRegionResponse(regionCode, RegionCode.nameOf(regionCode));
    }
}
