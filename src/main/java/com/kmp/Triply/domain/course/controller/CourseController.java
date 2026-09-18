package com.kmp.Triply.domain.course.controller;

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
import com.kmp.Triply.domain.course.service.CourseService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Course", description = "스토리텔링 코스/스팟/미션 관리 API")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "코스 생성", description = "새로운 스토리텔링 코스를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CourseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.createCourse(userId, request)));
    }

    @Operation(summary = "코스 목록 조회",
            description = "활성화된 코스 목록입니다. 지역·도시에 더해 실내 여부(indoorType)와 태그(tags)로 좁힐 수 있습니다. "
                    + "tags 를 여러 개 넣으면 그 태그를 모두 가진 코스만 나옵니다. "
                    + "예: indoorType=INDOOR 은 실내로만 도는 코스, tags=RAINY_DAY,KID_FRIENDLY 는 비 오는 날 아이와 갈 코스.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses(
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String city,
            @Parameter(description = "INDOOR(전부 실내) / MIXED(실내+야외) / OUTDOOR(전부 야외)", example = "INDOOR")
            @RequestParam(required = false) IndoorType indoorType,
            @Parameter(description = "모두 만족해야 하는 태그 목록", example = "RAINY_DAY,MUSEUM")
            @RequestParam(required = false) Set<CourseTag> tags) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourses(regionCode, city, indoorType, tags)));
    }

    @Operation(summary = "날씨로 코스 추천",
            description = "지금 날씨에 맞는 코스를 추천 순서대로 돌려줍니다. "
                    + "RAIN·SNOW·HOT·COLD 면 전부 야외인 코스는 제외하고, 실내 코스와 해당 상황 태그가 붙은 코스를 앞에 세웁니다. "
                    + "CLEAR 면 야외 코스를 앞에 세웁니다. 날씨 조회는 앱이 하고 그 결과만 넘겨주세요.")
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> recommendCourses(
            @Parameter(description = "시도 코드(충남=44)", example = "44")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "현재 날씨", example = "RAIN")
            @RequestParam WeatherCondition weather) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.recommendCourses(regionCode, weather)));
    }

    @Operation(summary = "코스 지역 목록 조회", description = "활성화된 코스가 있는 지역 코드 목록만 조회합니다.")
    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<CourseRegionResponse>>> getCourseRegions() {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourseRegions()));
    }

    @Operation(summary = "코스 상세 조회", description = "코스에 속한 스팟과 미션(퀴즈)을 함께 조회합니다.")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourseDetail(courseId)));
    }

    @Operation(summary = "코스 삭제",
            description = "본인이 만든 코스를 비활성화합니다. 목록·지역 조회에서 즉시 빠지고, 이미 진행된 게임 기록은 유지됩니다.")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long courseId) {
        courseService.deleteCourse(userId, courseId);
        return ResponseEntity.ok(ApiResponse.ok(null));
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
