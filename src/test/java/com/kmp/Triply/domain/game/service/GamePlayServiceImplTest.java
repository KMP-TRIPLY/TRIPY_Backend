package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 채점·점수 계산만 검증한다. 나머지 의존성은 grade/scoreFor 경로에서 쓰이지 않아 null.
 */
class GamePlayServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GamePlayServiceImpl service =
            new GamePlayServiceImpl(null, null, null, null, null, null, null, MAPPER);

    private static final String CHOICES = """
            [{"label":"경복궁","value":"A","is_correct":true},
             {"label":"창덕궁","value":"B","is_correct":false}]
            """;

    @Test
    void 텍스트_퀴즈는_공백과_대소문자를_무시하고_채점한다() throws Exception {
        Mission mission = quiz(MissionType.QUIZ_TEXT, "Gyeongbokgung", null);

        assertThat(service.grade(mission, submit("  gyeongbokGUNG  ", null))).isTrue();
        assertThat(service.grade(mission, submit("창덕궁", null))).isFalse();
        assertThat(service.grade(mission, submit("   ", null))).isFalse();
        assertThat(service.grade(mission, submit(null, null))).isFalse();
    }

    @Test
    void 선택형_퀴즈는_정답_선택지의_value나_label만_인정한다() throws Exception {
        Mission mission = quiz(MissionType.QUIZ_CHOICE, null, CHOICES);

        assertThat(service.grade(mission, submit("A", null))).isTrue();
        assertThat(service.grade(mission, submit("경복궁", null))).isTrue();
        assertThat(service.grade(mission, submit("B", null))).isFalse();
        assertThat(service.grade(mission, submit("창덕궁", null))).isFalse();
        assertThat(service.grade(mission, submit(null, null))).isFalse();
    }

    @Test
    void 인증형_미션은_제출물_존재만_확인한다() throws Exception {
        Mission mission = quiz(MissionType.PHOTO, null, null);

        assertThat(service.grade(mission, submit(null, "https://s3/a.jpg"))).isTrue();
        assertThat(service.grade(mission, submit(null, "  "))).isFalse();
        assertThat(service.grade(mission, submit(null, null))).isFalse();
    }

    @Test
    void 힌트를_쓰면_감점되고_점수는_음수가_되지_않는다() {
        Mission normal = quiz(MissionType.QUIZ_TEXT, "x", null);   // base 300 / penalty 150
        assertThat(service.scoreFor(normal, false)).isEqualTo(300);
        assertThat(service.scoreFor(normal, true)).isEqualTo(150);

        Mission cheap = Mission.builder()
                .missionType(MissionType.QUIZ_TEXT).question("q").answer("x")
                .baseScore(100).hintPenalty(500).build();
        assertThat(service.scoreFor(cheap, true)).isZero();
    }

    private static Mission quiz(MissionType type, String answer, String choices) {
        return Mission.builder()
                .missionType(type)
                .question("문제")
                .answer(answer)
                .choices(choices)
                .hint("힌트")
                .baseScore(300)
                .hintPenalty(150)
                .build();
    }

    private static MissionSubmitRequest submit(String submittedValue, String photoUrl) throws Exception {
        return MAPPER.readValue("""
                {"teamId":1,"submittedValue":%s,"photoUrl":%s}
                """.formatted(json(submittedValue), json(photoUrl)), MissionSubmitRequest.class);
    }

    private static String json(String value) throws Exception {
        return value == null ? "null" : MAPPER.writeValueAsString(value);
    }
}