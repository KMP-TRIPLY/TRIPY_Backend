package com.kmp.Triply.domain.course.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MissionChoiceRequest {

    @NotBlank(message = "보기 라벨은 필수입니다.")
    private String label;

    @NotBlank(message = "보기 값은 필수입니다.")
    private String value;

    @JsonProperty("is_correct")
    private boolean correct;
}
