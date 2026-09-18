package com.kmp.Triply.domain.course.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.RegionCode;
import com.kmp.Triply.domain.course.WeatherCondition;
import com.kmp.Triply.domain.course.dto.request.CourseCreateRequest;
import com.kmp.Triply.domain.course.dto.request.CourseSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.request.MissionCreateRequest;
import com.kmp.Triply.domain.course.dto.request.TourismSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.response.CourseDetailResponse;
import com.kmp.Triply.domain.course.dto.response.CourseRegionResponse;
import com.kmp.Triply.domain.course.dto.response.CourseResponse;
import com.kmp.Triply.domain.course.dto.response.CourseSpotResponse;
import com.kmp.Triply.domain.course.dto.response.MissionChoiceResponse;
import com.kmp.Triply.domain.course.dto.response.MissionResponse;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.IndoorType;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import com.kmp.Triply.domain.tourism.repository.TourismSpotRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final MissionRepository missionRepository;
    private final TourismSpotRepository tourismSpotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CourseResponse createCourse(Long userId, CourseCreateRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .regionCode(resolveRegionCode(request))
                .city(request.getCity())
                .difficulty(request.getDifficulty())
                .estimatedMinutes(request.getEstimatedMinutes())
                .courseType(request.getCourseType())
                .indoorType(request.getIndoorType())
                .tags(request.getTags())
                .isAiGenerated(false)
                .createdBy(creator)
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }

    /**
     * 지역 코드를 정한다. 클라이언트는 시도 코드를 모르므로 보통 city 만 보내고,
     * 서버가 도시 이름으로 찾는다. 코드를 직접 보냈으면 그대로 쓴다.
     */
    private String resolveRegionCode(CourseCreateRequest request) {
        if (StringUtils.hasText(request.getRegionCode())) {
            if (!RegionCode.exists(request.getRegionCode())) {
                throw new CustomException(ErrorCode.REGION_NOT_RESOLVED);
            }
            return request.getRegionCode();
        }

        return RegionCode.resolve(request.getCity())
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_RESOLVED));
    }

    /**
     * 지역·도시는 쿼리로 좁히고, 실내 여부와 태그는 가져온 뒤 거른다.
     * 코스 카탈로그는 수십 건 규모라 이쪽이 조건 조합마다 쿼리를 늘리는 것보다 단순하다.
     */
    public List<CourseResponse> getCourses(String regionCode, String city,
                                           IndoorType indoorType, Set<CourseTag> tags) {
        Set<CourseTag> requiredTags = tags == null ? Set.of() : tags;
        return courseRepository.findActiveCourses(regionCode, city).stream()
                .filter(course -> indoorType == null || course.getIndoorType() == indoorType)
                .filter(course -> course.getTags().containsAll(requiredTags))
                .map(CourseResponse::from)
                .toList();
    }

    /**
     * 날씨에 맞는 코스 추천. 비·눈·폭염·한파에는 전부 야외인 코스를 빼고,
     * 남은 것 중 실내 비중이 높고 해당 상황 태그가 붙은 코스를 앞에 세운다.
     */
    public List<CourseResponse> recommendCourses(String regionCode, WeatherCondition weather) {
        WeatherCondition condition = weather == null ? WeatherCondition.CLEAR : weather;
        return courseRepository.findActiveCourses(regionCode, null).stream()
                .filter(course -> fitsWeather(course, condition))
                // 정렬은 안정적이라 점수가 같으면 조회 순서(최신순)가 그대로 유지된다.
                .sorted(Comparator.comparingInt((Course course) -> weatherScore(course, condition)).reversed())
                .map(CourseResponse::from)
                .toList();
    }

    private boolean fitsWeather(Course course, WeatherCondition weather) {
        return switch (weather) {
            case RAIN, SNOW, HOT, COLD -> course.getIndoorType() != IndoorType.OUTDOOR;
            case CLEAR -> true;
        };
    }

    private int weatherScore(Course course, WeatherCondition weather) {
        int score = 0;
        CourseTag weatherTag = weatherTag(weather);
        if (weatherTag != null && course.getTags().contains(weatherTag)) {
            score += 10;
        }
        score += switch (weather) {
            case RAIN, SNOW, HOT, COLD -> course.getIndoorType() == IndoorType.INDOOR ? 5 : 0;
            case CLEAR -> course.getIndoorType() == IndoorType.OUTDOOR ? 5 : 0;
        };
        return score;
    }

    private CourseTag weatherTag(WeatherCondition weather) {
        return switch (weather) {
            case RAIN, SNOW -> CourseTag.RAINY_DAY;
            case HOT -> CourseTag.HOT_DAY;
            case COLD -> CourseTag.COLD_DAY;
            case CLEAR -> null;
        };
    }

    public List<CourseRegionResponse> getCourseRegions() {
        return courseRepository.findActiveCourseRegionCodes().stream()
                .map(CourseRegionResponse::from)
                .toList();
    }

    public CourseDetailResponse getCourseDetail(Long courseId) {
        Course course = getCourse(courseId);

        List<CourseSpot> spots = courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(courseId);
        List<Long> spotIds = spots.stream().map(CourseSpot::getId).toList();
        Map<Long, List<MissionResponse>> missionsBySpotId = missionRepository
                .findAllByCourseSpotIdInOrderByIdAsc(spotIds).stream()
                .collect(Collectors.groupingBy(
                        mission -> mission.getCourseSpot().getId(),
                        Collectors.mapping(this::toMissionResponse, Collectors.toList())));

        List<CourseSpotResponse> spotResponses = spots.stream()
                .map(spot -> CourseSpotResponse.from(
                        spot, missionsBySpotId.getOrDefault(spot.getId(), Collections.emptyList())))
                .toList();

        return CourseDetailResponse.of(course, spotResponses);
    }

    @Transactional
    public CourseSpotResponse addCourseSpot(Long courseId, CourseSpotCreateRequest request) {
        Course course = getCourse(courseId);
        TourismSpot tourismSpot = resolveTourismSpot(request);

        CourseSpot courseSpot = CourseSpot.builder()
                .course(course)
                .tourismSpot(tourismSpot)
                .sequenceOrder(request.getSequenceOrder())
                .storyText(request.getStoryText())
                .lat(request.getLat())
                .lng(request.getLng())
                .radiusMeters(request.getRadiusMeters())
                .indoor(request.isIndoor())
                .build();

        return CourseSpotResponse.from(courseSpotRepository.save(courseSpot), Collections.emptyList());
    }

    @Transactional
    public MissionResponse addMission(Long courseId, Long spotId, MissionCreateRequest request) {
        CourseSpot courseSpot = courseSpotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_SPOT_NOT_FOUND));
        if (!courseSpot.getCourse().getId().equals(courseId)) {
            throw new CustomException(ErrorCode.COURSE_SPOT_NOT_FOUND);
        }

        Mission mission = Mission.builder()
                .courseSpot(courseSpot)
                .missionType(request.getMissionType())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .choices(writeChoicesAsJson(request))
                .hint(request.getHint())
                .hintPenalty(request.getHintPenalty())
                .baseScore(request.getBaseScore())
                .build();

        return toMissionResponse(missionRepository.save(mission));
    }

    /**
     * 실제로 지우지 않고 비활성화한다. 목록·지역 조회는 isActive=true 만 보므로 바로 사라지고,
     * 이미 이 코스로 플레이한 게임방·랭킹은 그대로 남는다(FK 가 걸려 있어 하드 삭제는 어차피 막힌다).
     */
    @Transactional
    public void deleteCourse(Long userId, Long courseId) {
        Course course = getCourse(courseId);

        if (course.getCreatedBy() == null || !course.getCreatedBy().getId().equals(userId)) {
            throw new CustomException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        course.deactivate();
    }

    private TourismSpot resolveTourismSpot(CourseSpotCreateRequest request) {
        if (request.getTourismSpotId() != null) {
            return tourismSpotRepository.findById(request.getTourismSpotId())
                    .orElseThrow(() -> new CustomException(ErrorCode.TOURISM_SPOT_NOT_FOUND));
        }

        TourismSpotCreateRequest newSpot = request.getNewTourismSpot();
        if (newSpot == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String contentId = StringUtils.hasText(newSpot.getOpenApiContentId())
                ? newSpot.getOpenApiContentId()
                : "MANUAL-" + UUID.randomUUID();

        return tourismSpotRepository.findByOpenApiContentId(contentId)
                .orElseGet(() -> tourismSpotRepository.save(TourismSpot.builder()
                        .openApiContentId(contentId)
                        .name(newSpot.getName())
                        .category(newSpot.getCategory())
                        .address(newSpot.getAddress())
                        .lat(newSpot.getLat())
                        .lng(newSpot.getLng())
                        .thumbnailUrl(newSpot.getThumbnailUrl())
                        .areaCode(newSpot.getAreaCode())
                        .rank(null)
                        .build()));
    }

    private String writeChoicesAsJson(MissionCreateRequest request) {
        if (request.getChoices() == null || request.getChoices().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(request.getChoices());
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private MissionResponse toMissionResponse(Mission mission) {
        return MissionResponse.from(mission, MissionChoiceResponse.listFrom(objectMapper, mission.getChoices()));
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));
    }
}
