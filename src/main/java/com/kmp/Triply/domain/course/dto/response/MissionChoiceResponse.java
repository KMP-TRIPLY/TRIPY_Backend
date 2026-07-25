package com.kmp.Triply.domain.course.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionChoiceResponse {

    private String label;
    private String value;

    @JsonProperty("is_correct")
    private boolean correct;
}
