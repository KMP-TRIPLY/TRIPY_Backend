package com.kmp.Triply.domain.course.dto.request;

import com.kmp.Triply.domain.course.entity.MissionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class MissionCreateRequest {

    @NotNull(message = "미션 타입은 필수입니다.")
    private MissionType missionType;

    @NotBlank(message = "질문 내용은 필수입니다.")
    private String question;

    private String answer;

    @Valid
    private List<MissionChoiceRequest> choices;

    private String hint;

    private int hintPenalty = 150;

    private int baseScore = 300;
}
