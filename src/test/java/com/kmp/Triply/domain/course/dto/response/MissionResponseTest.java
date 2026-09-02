package com.kmp.Triply.domain.course.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 코스 상세는 로그인만 하면 누구나 본다. 여기에 정답이 실리면 게임이 성립하지 않는다.
 * 반대로 채점은 저장된 choices JSON 의 is_correct 를 읽어야 하므로 역직렬화는 살아 있어야 한다.
 */
class MissionResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 응답에_정답이_실리지_않는다() throws Exception {
        MissionResponse response = MissionResponse.builder()
                .id(1L)
                .question("무령왕릉이 발견된 연도는?")
                .choices(List.of(
                        MissionChoiceResponse.builder().label("1").value("1971").correct(true).build(),
                        MissionChoiceResponse.builder().label("2").value("1985").correct(false).build()))
                .hint("70년대")
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).doesNotContain("is_correct").doesNotContain("answer");
        assertThat(json).contains("1971").contains("70년대");
    }

    @Test
    void 저장된_choices_JSON_의_정답_표시는_그대로_읽는다() throws Exception {
        String stored = """
                [{"label":"1","value":"1971","is_correct":true},
                 {"label":"2","value":"1985","is_correct":false}]""";

        List<MissionChoiceResponse> choices = objectMapper.readValue(
                stored,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MissionChoiceResponse.class));

        assertThat(choices).extracting(MissionChoiceResponse::isCorrect).containsExactly(true, false);
    }
}
