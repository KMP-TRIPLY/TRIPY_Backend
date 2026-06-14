package com.kmp.Triply.domain.place.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PlaceCreateRequest {

    @NotBlank(message = "장소 이름은 필수입니다.")
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private LocalDate visitDate;

    private String memo;
}