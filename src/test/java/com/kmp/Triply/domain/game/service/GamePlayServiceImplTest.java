package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 채점·점수 계산만 검증한다. 나머지 의존성은 grade/scoreFor 경로에서 쓰이지 않아 null.
 */
class GamePlayServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GamePlayServiceImpl service =
            new GamePlayServiceImpl(null, null, null, null, null, null, null, MAPPER, null, null);

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
    void 사진_계열은_문자열_제출로_통과시키지_않는다() throws Exception {
        // 예전에는 photoUrl 에 아무 문자열이나 넣으면 통과했다
        for (MissionType type : new MissionType[]{MissionType.PHOTO, MissionType.AR, MissionType.VOICE}) {
            Mission mission = quiz(type, null, null);
            assertThatThrownBy(() -> service.grade(mission, submit(null, "https://s3/a.jpg")))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PHOTO_SUBMIT_REQUIRED);
            assertThat(GamePlayServiceImpl.requiresPhotoUpload(type)).isTrue();
        }
    }

    @Test
    void NFC_는_태그값_제출로_채점한다() throws Exception {
        Mission mission = quiz(MissionType.NFC, null, null);

        assertThat(service.grade(mission, submit("tag-1234", null))).isTrue();
        assertThat(service.grade(mission, submit("  ", null))).isFalse();
        assertThat(service.grade(mission, submit(null, null))).isFalse();
        assertThat(GamePlayServiceImpl.requiresPhotoUpload(MissionType.NFC)).isFalse();
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
                {"roomId":1,"submittedValue":%s,"photoUrl":%s}
                """.formatted(json(submittedValue), json(photoUrl)), MissionSubmitRequest.class);
    }

    private static String json(String value) throws Exception {
        return value == null ? "null" : MAPPER.writeValueAsString(value);
    }
}