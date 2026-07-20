package com.kmp.Triply.domain.course.dto.request;

import com.kmp.Triply.domain.tourism.entity.SpotCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TourismSpotCreateRequest {

    private String openApiContentId;

    @NotBlank(message = "관광지 이름은 필수입니다.")
    private String name;

    private SpotCategory category;

    private String address;

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;

    private String thumbnailUrl;

    private String areaCode;
}
