package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MissionResponse {

    private Long id;
    private MissionType missionType;
    private String question;
    private String answer;
    private List<MissionChoiceResponse> choices;
    private String hint;
    private int hintPenalty;
    private int baseScore;

    public static MissionResponse from(Mission mission, List<MissionChoiceResponse> choices) {
        return MissionResponse.builder()
                .id(mission.getId())
                .missionType(mission.getMissionType())
                .question(mission.getQuestion())
                .answer(mission.getAnswer())
                .choices(choices)
                .hint(mission.getHint())
                .hintPenalty(mission.getHintPenalty())
                .baseScore(mission.getBaseScore())
                .build();
    }
}
