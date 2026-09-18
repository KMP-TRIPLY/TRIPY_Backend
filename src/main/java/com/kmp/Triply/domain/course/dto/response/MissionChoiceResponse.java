package com.kmp.Triply.domain.course.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

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

    /** 저장된 choices JSON 을 보기 목록으로. 코스 조회와 게임 채점이 같은 방식으로 읽어야 한다. */
    public static List<MissionChoiceResponse> listFrom(ObjectMapper objectMapper, String choicesJson) {
        if (!StringUtils.hasText(choicesJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    choicesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MissionChoiceResponse.class));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
