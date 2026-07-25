package com.kmp.Triply.domain.course.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.dto.request.CourseCreateRequest;
import com.kmp.Triply.domain.course.dto.request.CourseSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.request.MissionCreateRequest;
import com.kmp.Triply.domain.course.dto.request.TourismSpotCreateRequest;
import com.kmp.Triply.domain.course.dto.response.CourseDetailResponse;
import com.kmp.Triply.domain.course.dto.response.CourseResponse;
import com.kmp.Triply.domain.course.dto.response.CourseSpotResponse;
import com.kmp.Triply.domain.course.dto.response.MissionChoiceResponse;
import com.kmp.Triply.domain.course.dto.response.MissionResponse;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseSpot;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final MissionRepository missionRepository;
    private final TourismSpotRepository tourismSpotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CourseResponse createCourse(Long userId, CourseCreateRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .regionCode(request.getRegionCode())
                .city(request.getCity())
                .difficulty(request.getDifficulty())
                .estimatedMinutes(request.getEstimatedMinutes())
                .courseType(request.getCourseType())
                .isAiGenerated(false)
                .createdBy(creator)
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }

    @Override
    public List<CourseResponse> getCourses(String regionCode, String city) {
        return courseRepository.findActiveCourses(regionCode, city).stream()
                .map(CourseResponse::from)
                .toList();
    }

    @Override
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

    @Override
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
                .build();

        return CourseSpotResponse.from(courseSpotRepository.save(courseSpot), Collections.emptyList());
    }

    @Override
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
        return MissionResponse.from(mission, readChoices(mission.getChoices()));
    }

    private List<MissionChoiceResponse> readChoices(String choicesJson) {
        if (!StringUtils.hasText(choicesJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(
                    choicesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MissionChoiceResponse.class));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));
    }
}
