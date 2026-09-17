package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.course.entity.MissionType;

import java.util.List;

/** 시드 미션 생성 헬퍼. 점수/힌트 패널티 기본값을 미션 유형별로 통일한다. */
final class SeedMissions {

    private SeedMissions() {
    }

    static MissionDef choiceQuiz(String question, String hint, ChoiceDef... choices) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null, List.of(choices), hint, 150, 300);
    }

    static MissionDef oxQuiz(String question, boolean answerIsO, String hint) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null,
                List.of(choice("O", "O", answerIsO), choice("X", "X", !answerIsO)), hint, 100, 200);
    }

    static MissionDef textQuiz(String question, String answer) {
        return new MissionDef(MissionType.QUIZ_TEXT, question, answer, null, null, 150, 300);
    }

    static MissionDef textQuiz(String question, String answer, String hint) {
        return new MissionDef(MissionType.QUIZ_TEXT, question, answer, null, hint, 150, 300);
    }

    static MissionDef photo(String question) {
        return new MissionDef(MissionType.PHOTO, question, null, null, null, 0, 100);
    }

    static ChoiceDef choice(String label, String value, boolean correct) {
        return new ChoiceDef(label, value, correct);
    }
}
