package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 코스 상세 · 미션 등록 응답. 정답(answer)과 선택지 정답 여부는 담지 않는다 —
 * 코스는 로그인만 하면 누구나 조회할 수 있어서 정답을 실으면 게임이 성립하지 않는다.
 */
@Getter
@Builder
public class MissionResponse {

    private Long id;
    private MissionType missionType;
    private String question;
    private List<MissionChoiceResponse> choices;
    private String hint;
    private int hintPenalty;
    private int baseScore;

    public static MissionResponse from(Mission mission, List<MissionChoiceResponse> choices) {
        return MissionResponse.builder()
                .id(mission.getId())
                .missionType(mission.getMissionType())
                .question(mission.getQuestion())
                .choices(choices)
                .hint(mission.getHint())
                .hintPenalty(mission.getHintPenalty())
                .baseScore(mission.getBaseScore())
                .build();
    }
}
