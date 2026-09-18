package com.kmp.Triply.domain.course.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.WeatherCondition;
import com.kmp.Triply.domain.course.dto.response.CourseResponse;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.CourseType;
import com.kmp.Triply.domain.course.entity.Difficulty;
import com.kmp.Triply.domain.course.entity.IndoorType;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseWeatherRecommendTest {

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CourseService courseService;

    private final Course indoorCourse = course("비 오는 날의 사비", IndoorType.INDOOR,
            Set.of(CourseTag.RAINY_DAY, CourseTag.MUSEUM, CourseTag.KID_FRIENDLY));
    private final Course mixedCourse = course("사비의 마지막 하루", IndoorType.MIXED,
            Set.of(CourseTag.HISTORY));
    private final Course outdoorCourse = course("하루 만에 서해 낙조를 훔쳐라", IndoorType.OUTDOOR,
            Set.of(CourseTag.SUNSET));

    @Test
    void 비가_오면_전부_야외인_코스는_빠지고_실내_코스가_먼저_온다() {
        when(courseRepository.findActiveCourses(any(), any()))
                .thenReturn(List.of(outdoorCourse, mixedCourse, indoorCourse));

        List<CourseResponse> recommended = courseService.recommendCourses("44", WeatherCondition.RAIN);

        assertThat(recommended).extracting(CourseResponse::getTitle)
                .containsExactly("비 오는 날의 사비", "사비의 마지막 하루");
    }

    @Test
    void 맑은_날에는_야외_코스를_앞에_세우고_아무것도_빼지_않는다() {
        when(courseRepository.findActiveCourses(any(), any()))
                .thenReturn(List.of(indoorCourse, mixedCourse, outdoorCourse));

        List<CourseResponse> recommended = courseService.recommendCourses("44", WeatherCondition.CLEAR);

        assertThat(recommended).extracting(CourseResponse::getTitle)
                .containsExactly("하루 만에 서해 낙조를 훔쳐라", "비 오는 날의 사비", "사비의 마지막 하루");
    }

    @Test
    void 태그를_여러_개_넣으면_모두_가진_코스만_남는다() {
        when(courseRepository.findActiveCourses("44", null))
                .thenReturn(List.of(outdoorCourse, mixedCourse, indoorCourse));

        List<CourseResponse> filtered = courseService.getCourses("44", null, null,
                Set.of(CourseTag.RAINY_DAY, CourseTag.KID_FRIENDLY));

        assertThat(filtered).extracting(CourseResponse::getTitle)
                .containsExactly("비 오는 날의 사비");
    }

    @Test
    void 실내_코스만_골라낼_수_있다() {
        when(courseRepository.findActiveCourses("44", null))
                .thenReturn(List.of(outdoorCourse, mixedCourse, indoorCourse));

        List<CourseResponse> filtered = courseService.getCourses("44", null, IndoorType.INDOOR, null);

        assertThat(filtered).extracting(CourseResponse::getTitle)
                .containsExactly("비 오는 날의 사비");
    }

    private static Course course(String title, IndoorType indoorType, Set<CourseTag> tags) {
        return Course.builder()
                .title(title)
                .description(title)
                .regionCode("44")
                .city("부여")
                .difficulty(Difficulty.EASY)
                .estimatedMinutes(300)
                .courseType(CourseType.GENERAL)
                .indoorType(indoorType)
                .tags(tags)
                .isAiGenerated(false)
                .build();
    }
}
