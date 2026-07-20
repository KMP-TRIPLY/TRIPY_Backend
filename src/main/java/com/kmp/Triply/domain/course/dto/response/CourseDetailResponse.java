package com.kmp.Triply.domain.course.dto.response;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseType;
import com.kmp.Triply.domain.course.entity.Difficulty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String regionCode;
    private String city;
    private Difficulty difficulty;
    private int estimatedMinutes;
    private CourseType courseType;
    private boolean isActive;
    private List<CourseSpotResponse> spots;

    public static CourseDetailResponse of(Course course, List<CourseSpotResponse> spots) {
        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .regionCode(course.getRegionCode())
                .city(course.getCity())
                .difficulty(course.getDifficulty())
                .estimatedMinutes(course.getEstimatedMinutes())
                .courseType(course.getCourseType())
                .isActive(course.isActive())
                .spots(spots)
                .build();
    }
}
