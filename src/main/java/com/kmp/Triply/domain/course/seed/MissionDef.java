package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.course.entity.MissionType;

import java.util.List;

/** 스팟 하나에 붙는 미션 정의. {@link SeedMissions} 의 헬퍼로 생성한다. */
record MissionDef(
        MissionType type, String question, String answer,
        List<ChoiceDef> choices, String hint, int hintPenalty, int baseScore) {
}
