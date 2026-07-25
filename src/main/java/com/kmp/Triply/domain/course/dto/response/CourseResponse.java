package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseType;
import com.kmp.Triply.domain.course.entity.Difficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String regionCode;
    private String city;
    private Difficulty difficulty;
    private int estimatedMinutes;
    private CourseType courseType;
    private boolean isActive;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .regionCode(course.getRegionCode())
                .city(course.getCity())
                .difficulty(course.getDifficulty())
                .estimatedMinutes(course.getEstimatedMinutes())
                .courseType(course.getCourseType())
                .isActive(course.isActive())
                .build();
    }
}
