package com.kmp.Triply.domain.course.seed;

/** 객관식 보기 하나. {@code [{label, value, is_correct}]} JSON 으로 직렬화된다. */
record ChoiceDef(String label, String value, boolean correct) {
}
