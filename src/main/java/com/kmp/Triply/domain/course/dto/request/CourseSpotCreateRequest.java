package com.kmp.Triply.domain.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CourseSpotCreateRequest {

    private Long tourismSpotId;

    @Valid
    private TourismSpotCreateRequest newTourismSpot;

    @NotNull(message = "코스 내 순서는 필수입니다.")
    private Short sequenceOrder;

    private String storyText;

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;

    private int radiusMeters = 200;
}
