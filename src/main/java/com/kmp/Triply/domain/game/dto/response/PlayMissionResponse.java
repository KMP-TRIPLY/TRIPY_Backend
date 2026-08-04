package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.course.entity.MissionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 게임 진행 중 팀에게 내려주는 미션. 정답(answer) 및 선택지 정답 여부는 포함하지 않는다.
 */
@Getter
@Builder
public class PlayMissionResponse {

    private Long id;
    private MissionType missionType;
    private String question;
    private List<PlayChoiceResponse> choices;
    private int baseScore;
    private int hintPenalty;
    private boolean solved;

    public static PlayMissionResponse of(Long id, MissionType missionType, String question,
                                         List<PlayChoiceResponse> choices, int baseScore,
                                         int hintPenalty, boolean solved) {
        return PlayMissionResponse.builder()
                .id(id)
                .missionType(missionType)
                .question(question)
                .choices(choices)
                .baseScore(baseScore)
                .hintPenalty(hintPenalty)
                .solved(solved)
                .build();
    }
}
