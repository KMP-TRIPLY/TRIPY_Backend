package com.kmp.Triply.domain.course.controller;

import com.kmp.Triply.domain.course.dto.request.CourseCreateRequest;
import com.kmp.Triply.domain.course.dto.request.CourseSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.request.MissionCreateRequest;
import com.kmp.Triply.domain.course.dto.response.CourseDetailResponse;
import com.kmp.Triply.domain.course.dto.response.CourseResponse;
import com.kmp.Triply.domain.course.dto.response.CourseSpotResponse;
import com.kmp.Triply.domain.course.dto.response.MissionResponse;
import com.kmp.Triply.domain.course.service.CourseService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Course", description = "스토리텔링 코스/스팟/미션 관리 API")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "코스 생성", description = "새로운 스토리텔링 코스를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            Authentication authentication,
            @Valid @RequestBody CourseCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.createCourse(userId, request)));
    }

    @Operation(summary = "코스 목록 조회", description = "지역/도시로 필터링된 활성화된 코스 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses(
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourses(regionCode, city)));
    }

    @Operation(summary = "코스 상세 조회", description = "코스에 속한 스팟과 미션(퀴즈)을 함께 조회합니다.")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourseDetail(courseId)));
    }

    @Operation(summary = "코스 스팟 추가", description = "코스에 방문 스팟을 추가합니다. 기존 관광지를 참조하거나 새 관광지를 함께 생성할 수 있습니다.")
    @PostMapping("/{courseId}/spots")
    public ResponseEntity<ApiResponse<CourseSpotResponse>> addCourseSpot(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseSpotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.addCourseSpot(courseId, request)));
    }

    @Operation(summary = "미션(퀴즈) 추가", description = "코스 스팟에 GPS 인증, 퀴즈, 사진 인증 등의 미션을 추가합니다.")
    @PostMapping("/{courseId}/spots/{spotId}/missions")
    public ResponseEntity<ApiResponse<MissionResponse>> addMission(
            @PathVariable Long courseId,
            @PathVariable Long spotId,
            @Valid @RequestBody MissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.addMission(courseId, spotId, request)));
    }
}
