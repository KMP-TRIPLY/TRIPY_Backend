package com.kmp.Triply.domain.course.service;

import com.kmp.Triply.domain.course.dto.request.CourseCreateRequest;
import com.kmp.Triply.domain.course.dto.request.CourseSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.request.MissionCreateRequest;
import com.kmp.Triply.domain.course.dto.response.CourseDetailResponse;
import com.kmp.Triply.domain.course.dto.response.CourseRegionResponse;
import com.kmp.Triply.domain.course.dto.response.CourseResponse;
import com.kmp.Triply.domain.course.dto.response.CourseSpotResponse;
import com.kmp.Triply.domain.course.dto.response.MissionResponse;
import com.kmp.Triply.domain.course.WeatherCondition;
import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.IndoorType;

import java.util.List;
import java.util.Set;

public interface CourseService {

    CourseResponse createCourse(Long userId, CourseCreateRequest request);

    List<CourseResponse> getCourses(String regionCode, String city, IndoorType indoorType, Set<CourseTag> tags);

    List<CourseResponse> recommendCourses(String regionCode, WeatherCondition weather);

    List<CourseRegionResponse> getCourseRegions();

    CourseDetailResponse getCourseDetail(Long courseId);

    CourseSpotResponse addCourseSpot(Long courseId, CourseSpotCreateRequest request);

    MissionResponse addMission(Long courseId, Long spotId, MissionCreateRequest request);

    void deleteCourse(Long userId, Long courseId);
}
