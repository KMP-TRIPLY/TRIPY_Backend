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

    /**
     * 저장된 choices JSON 을 읽을 때만 쓴다(채점용). 응답으로 나가면 코스 상세만 열어봐도
     * 정답을 알 수 있으므로 직렬화에서는 제외한다.
     */
    @JsonProperty(value = "is_correct", access = JsonProperty.Access.WRITE_ONLY)
    private boolean correct;
}
