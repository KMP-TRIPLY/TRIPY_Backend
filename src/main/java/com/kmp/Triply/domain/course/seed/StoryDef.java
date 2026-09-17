package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.Difficulty;
import com.kmp.Triply.domain.course.entity.IndoorType;

import java.util.List;
import java.util.Set;

/** 코스 한 개(= 스토리 한 편)의 정의. */
record StoryDef(
        String title, String description, String city,
        Difficulty difficulty, int estimatedMinutes,
        IndoorType indoorType, Set<CourseTag> tags, List<SpotDef> spots) {
}
