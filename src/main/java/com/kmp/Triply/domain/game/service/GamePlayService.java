package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.dto.response.MissionChoiceResponse;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.game.dto.request.HintRequest;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.request.SpotArriveRequest;
import com.kmp.Triply.domain.game.dto.response.MissionAttemptEventResponse;
import com.kmp.Triply.domain.game.dto.response.HintResponse;
import com.kmp.Triply.domain.game.dto.response.MissionSubmitResponse;
import com.kmp.Triply.domain.game.dto.response.PlayChoiceResponse;
import com.kmp.Triply.domain.game.dto.response.PlayMissionResponse;
import com.kmp.Triply.domain.game.dto.response.SpotArriveResponse;
import com.kmp.Triply.domain.game.dto.response.SpotCompletedEventResponse;
import com.kmp.Triply.domain.game.dto.response.SpotProgressResponse;
import com.kmp.Triply.domain.game.dto.response.RoomProgressResponse;
import com.kmp.Triply.domain.game.entity.AttemptResult;
import com.kmp.Triply.domain.game.entity.AttemptType;
import com.kmp.Triply.domain.game.entity.GameProgress;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.MissionAttempt;
import com.kmp.Triply.domain.game.entity.ProgressStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.GameProgressRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.service.NotificationService;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamePlayService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GameProgressRepository gameProgressRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final MissionRepository missionRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final GameRoomRealtimeNotifier realtimeNotifier;
    private final ObjectMapper objectMapper;
    private final PhotoStorageService photoStorage;
    private final PhotoVerificationService photoVerifier;
    private final NotificationService notificationService;

    public RoomProgressResponse getRoomProgress(Long userId, Long roomId) {
        Team team = teamRepository.findOfRoom(roomId);
        validateRoomMember(roomId, userId);

        Long courseId = team.getGameRoom().getCourse().getId();
        List<CourseSpot> spots = courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(courseId);

        List<SpotProgressResponse> spotProgresses = spots.stream()
                .map(spot -> toSpotProgress(team.getId(), spot))
                .toList();

        return RoomProgressResponse.of(team, spotProgresses);
    }

    @Transactional
    public SpotArriveResponse arriveSpot(Long userId, Long roomId, Long spotId, SpotArriveRequest request) {
        Team team = teamRepository.findOfRoom(roomId);
        teamMemberUser(team.getId(), userId);
        GameRoom room = team.getGameRoom();
        validateRunning(room);

        CourseSpot spot = getCourseSpotInCourse(spotId, room.getCourse().getId());
        validateSequentialOrder(team.getId(), room.getCourse().getId(), spot);

        Integer distanceMeters = null;
        if (!request.isSkipGps()) {
            if (request.getLat() == null || request.getLng() == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            double distance = haversineMeters(spot.getLat(), spot.getLng(), request.getLat(), request.getLng());
            if (distance > spot.getRadiusMeters()) {
                throw new CustomException(ErrorCode.OUT_OF_RANGE);
            }
            distanceMeters = (int) Math.round(distance);
        }

        GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(team.getId(), spotId)
                .orElseGet(() -> gameProgressRepository.save(GameProgress.builder()
                        .team(team)
                        .courseSpot(spot)
                        .status(ProgressStatus.LOCKED)
                        .build()));
        if (progress.getStatus() == ProgressStatus.LOCKED) {
            progress.activate();
        }

        int missionCount = missionRepository.findAllByCourseSpotIdOrderByIdAsc(spotId).size();
        realtimeNotifier.publish(roomId, "SPOT_ACTIVATED",
                team.getTeamName() + " 팀이 스팟에 도착했습니다.", SpotArriveResponse.of(progress, distanceMeters, missionCount));
        return SpotArriveResponse.of(progress, distanceMeters, missionCount);
    }

    public List<PlayMissionResponse> getSpotMissions(Long userId, Long roomId, Long spotId) {
        Team team = teamRepository.findOfRoom(roomId);
        validateRoomMember(roomId, userId);

        CourseSpot spot = getCourseSpotInCourse(spotId, team.getGameRoom().getCourse().getId());
        GameProgress progress = getActiveOrCompletedProgress(team.getId(), spot.getId());

        Set<Long> solvedMissionIds = missionAttemptRepository
                .findAllByGameProgressIdAndResult(progress.getId(), AttemptResult.CORRECT).stream()
                .map(attempt -> attempt.getMission().getId())
                .collect(Collectors.toSet());

        return missionRepository.findAllByCourseSpotIdOrderByIdAsc(spotId).stream()
                .map(mission -> PlayMissionResponse.of(
                        mission.getId(),
                        mission.getMissionType(),
                        mission.getQuestion(),
                        toPlayChoices(mission.getChoices()),
                        mission.getBaseScore(),
                        mission.getHintPenalty(),
                        solvedMissionIds.contains(mission.getId())))
                .toList();
    }

    @Transactional
    public HintResponse requestHint(Long userId, Long missionId, HintRequest request) {
        Mission mission = getMission(missionId);
        Team team = teamRepository.findOfRoom(request.getRoomId());
        User requester = teamMemberUser(team.getId(), userId);
        validateRunning(team.getGameRoom());
        validateMissionInCourse(mission, team.getGameRoom().getCourse().getId());

        GameProgress progress = getActiveProgress(team.getId(), mission.getCourseSpot().getId());
        if (missionAttemptRepository.existsByGameProgressIdAndMissionIdAndResult(
                progress.getId(), missionId, AttemptResult.CORRECT)) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_SOLVED);
        }

        boolean alreadyRequested = missionAttemptRepository.existsByGameProgressIdAndMissionIdAndAttemptType(
                progress.getId(), missionId, AttemptType.HINT_REQUEST);
        if (!alreadyRequested) {
            missionAttemptRepository.save(MissionAttempt.builder()
                    .gameProgress(progress)
                    .mission(mission)
                    .user(requester)
                    .attemptType(AttemptType.HINT_REQUEST)
                    .result(AttemptResult.PENDING)
                    .scoreEarned(0)
                    .hintUsed(true)
                    .build());
            team.useHint();
        }

        return HintResponse.builder()
                .missionId(missionId)
                .hint(mission.getHint())
                .hintPenalty(mission.getHintPenalty())
                .build();
    }

    @Transactional
    public MissionSubmitResponse submitMission(Long userId, Long missionId, MissionSubmitRequest request) {
        Mission mission = getMission(missionId);
        // 사진 계열은 문자열만 보내면 통과하던 구멍이 있었다. 실제 이미지를 받는 전용 경로로만 받는다.
        if (requiresPhotoUpload(mission.getMissionType())) {
            throw new CustomException(ErrorCode.PHOTO_SUBMIT_REQUIRED);
        }
        Submission submission = validateSubmission(userId, missionId, request.getRoomId(), mission);
        return record(submission, grade(mission, request), request.getSubmittedValue(), null, null);
    }

    /** 사진 계열 미션 제출. 이미지 자체를 받아 저장하고 판정한다. */
    @Transactional
    public MissionSubmitResponse submitPhotoMission(Long userId, Long missionId, Long roomId,
                                                    byte[] image, String contentType) {
        Mission mission = getMission(missionId);
        if (!requiresPhotoUpload(mission.getMissionType())) {
            throw new CustomException(ErrorCode.NOT_PHOTO_MISSION);
        }
        // 업로드·AI 호출은 비싸다. 팀원·진행중·도착인증·중복 검사를 모두 통과한 뒤에만 부른다.
        Submission submission = validateSubmission(userId, missionId, roomId, mission);

        // 판정을 못 하면 503 으로 돌려보내므로 판정을 먼저 한다 — 그래야 아무 데도 안 쓰이는 사진이 저장소에 남지 않는다.
        // 오답으로 판정돼도 사진은 올린다. 이의 제기 때 원본이 있어야 한다.
        PhotoVerdict verdict = photoVerifier.verify(image, contentType, mission);
        String photoKey = photoStorage.upload(image, contentType, missionId, submission.team().getId());
        return record(submission, verdict.passed(), null, photoKey, verdict.note());
    }

    static boolean requiresPhotoUpload(MissionType type) {
        return type == MissionType.PHOTO || type == MissionType.AR || type == MissionType.VOICE;
    }

    /** 제출 전 공통 검사. 통과하면 채점에 필요한 것들을 묶어 돌려준다. */
    private Submission validateSubmission(Long userId, Long missionId, Long roomId, Mission mission) {
        Team team = teamRepository.findOfRoom(roomId);
        User submitter = teamMemberUser(team.getId(), userId);
        GameRoom room = team.getGameRoom();
        validateRunning(room);
        validateMissionInCourse(mission, room.getCourse().getId());

        CourseSpot spot = mission.getCourseSpot();
        GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(team.getId(), spot.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_ACTIVE));

        // 이미 정답 처리된 미션은 스팟 완료 여부와 무관하게 중복 제출을 차단한다(일관된 409).
        if (missionAttemptRepository.existsByGameProgressIdAndMissionIdAndResult(
                progress.getId(), missionId, AttemptResult.CORRECT)) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_SOLVED);
        }
        if (progress.getStatus() != ProgressStatus.ACTIVE) {
            throw new CustomException(ErrorCode.SPOT_NOT_ACTIVE);
        }

        boolean hintUsed = missionAttemptRepository.existsByGameProgressIdAndMissionIdAndAttemptType(
                progress.getId(), missionId, AttemptType.HINT_REQUEST);
        return new Submission(submitter, mission, team, room, spot, progress, hintUsed);
    }

    private record Submission(User user, Mission mission, Team team, GameRoom room,
                              CourseSpot spot, GameProgress progress, boolean hintUsed) {}

    private MissionSubmitResponse record(Submission submission, boolean correct,
                                         String submittedValue, String photoKey, String verificationNote) {
        Long missionId = submission.mission().getId();
        Mission mission = submission.mission();
        Team team = submission.team();
        GameRoom room = submission.room();
        CourseSpot spot = submission.spot();
        GameProgress progress = submission.progress();
        boolean hintUsed = submission.hintUsed();

        AttemptResult result = correct ? AttemptResult.CORRECT : AttemptResult.WRONG;
        int scoreEarned = correct ? scoreFor(mission, hintUsed) : 0;

        missionAttemptRepository.save(MissionAttempt.builder()
                .gameProgress(progress)
                .mission(mission)
                .user(submission.user())
                .attemptType(AttemptType.SUBMIT)
                .submittedValue(submittedValue)
                .photoUrl(photoKey)
                .verificationNote(verificationNote)
                .result(result)
                .scoreEarned(scoreEarned)
                .hintUsed(hintUsed)
                .build());

        boolean spotCompleted = false;
        Long nextSpotId = null;
        if (correct) {
            team.addScore(scoreEarned);

            long solvedCount = missionAttemptRepository.countByGameProgressIdAndResult(
                    progress.getId(), AttemptResult.CORRECT);
            long totalMissions = missionRepository.findAllByCourseSpotIdOrderByIdAsc(spot.getId()).size();
            if (solvedCount >= totalMissions) {
                progress.complete();
                spotCompleted = true;
                nextSpotId = nextSpotId(room.getCourse().getId(), spot.getSequenceOrder());
                realtimeNotifier.publish(room.getId(), "SPOT_COMPLETED",
                        team.getTeamName() + " 팀이 스팟을 완료했습니다.",
                        SpotCompletedEventResponse.of(spot.getId(), nextSpotId));
            }
            realtimeNotifier.publish(room.getId(), "SCORE_UPDATED",
                    team.getTeamName() + " 팀 점수가 갱신되었습니다.", team.getTotalScore());
            notificationService.createMissionClearNotifications(team, submission.user(), scoreEarned);
        }
        // 정답·오답을 이벤트 이름으로 갈라 보낸다. 예전에는 둘 다 MISSION_SOLVED 로 나가고
        // 구분할 단서가 사람이 읽는 문장뿐이라 프론트가 한글을 파싱해야 했다.
        realtimeNotifier.publish(room.getId(),
                correct ? "MISSION_SOLVED" : "MISSION_FAILED",
                submission.user().getNickname() + "님의 미션 결과: " + result.name(),
                MissionAttemptEventResponse.of(
                        missionId, spot.getId(), result, scoreEarned, hintUsed,
                        submission.user().getId(), submission.user().getNickname(),
                        team.getTotalScore(), spotCompleted));

        return MissionSubmitResponse.builder()
                .missionId(missionId)
                .result(result)
                .correct(correct)
                .scoreEarned(scoreEarned)
                .hintUsed(hintUsed)
                .teamTotalScore(team.getTotalScore())
                .spotCompleted(spotCompleted)
                .nextSpotId(nextSpotId)
                .build();
    }

    // ===== 채점 =====

    int scoreFor(Mission mission, boolean hintUsed) {
        return Math.max(0, mission.getBaseScore() - (hintUsed ? mission.getHintPenalty() : 0));
    }

    boolean grade(Mission mission, MissionSubmitRequest request) {
        return switch (mission.getMissionType()) {
            case QUIZ_TEXT -> matchesText(request.getSubmittedValue(), mission.getAnswer());
            case QUIZ_CHOICE -> matchesCorrectChoice(mission.getChoices(), request.getSubmittedValue());
            // NFC 는 태그 값을 제출한다. 사진 계열은 여기로 오지 않는다 (submitMission 에서 막힘).
            case NFC -> StringUtils.hasText(request.getSubmittedValue());
            case PHOTO, AR, VOICE -> throw new CustomException(ErrorCode.PHOTO_SUBMIT_REQUIRED);
        };
    }

    private boolean matchesText(String submitted, String answer) {
        if (!StringUtils.hasText(submitted) || !StringUtils.hasText(answer)) {
            return false;
        }
        return submitted.trim().equalsIgnoreCase(answer.trim());
    }

    private boolean matchesCorrectChoice(String choicesJson, String submitted) {
        if (!StringUtils.hasText(submitted)) {
            return false;
        }
        String normalized = submitted.trim();
        return MissionChoiceResponse.listFrom(objectMapper, choicesJson).stream()
                .filter(MissionChoiceResponse::isCorrect)
                .anyMatch(choice -> normalized.equalsIgnoreCase(choice.getValue())
                        || normalized.equalsIgnoreCase(choice.getLabel()));
    }

    private List<PlayChoiceResponse> toPlayChoices(String choicesJson) {
        return MissionChoiceResponse.listFrom(objectMapper, choicesJson).stream()
                .map(choice -> PlayChoiceResponse.builder()
                        .label(choice.getLabel())
                        .value(choice.getValue())
                        .build())
                .toList();
    }

    // ===== 진행/검증 헬퍼 =====

    private SpotProgressResponse toSpotProgress(Long teamId, CourseSpot spot) {
        GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(teamId, spot.getId()).orElse(null);
        int totalMissions = missionRepository.findAllByCourseSpotIdOrderByIdAsc(spot.getId()).size();
        int solved = progress == null ? 0
                : (int) missionAttemptRepository.countByGameProgressIdAndResult(progress.getId(), AttemptResult.CORRECT);
        return SpotProgressResponse.builder()
                .spotId(spot.getId())
                .sequenceOrder(spot.getSequenceOrder())
                .status(progress == null ? ProgressStatus.LOCKED : progress.getStatus())
                .totalMissions(totalMissions)
                .solvedMissions(solved)
                .build();
    }

    private void validateSequentialOrder(Long teamId, Long courseId, CourseSpot target) {
        List<CourseSpot> spots = courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(courseId);
        for (CourseSpot spot : spots) {
            if (spot.getSequenceOrder() >= target.getSequenceOrder()) {
                break;
            }
            GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(teamId, spot.getId()).orElse(null);
            if (progress == null || progress.getStatus() != ProgressStatus.COMPLETED) {
                throw new CustomException(ErrorCode.SPOT_LOCKED);
            }
        }
    }

    private Long nextSpotId(Long courseId, short currentSequenceOrder) {
        return courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(courseId).stream()
                .filter(spot -> spot.getSequenceOrder() > currentSequenceOrder)
                .map(CourseSpot::getId)
                .findFirst()
                .orElse(null);
    }

    private GameProgress getActiveProgress(Long teamId, Long spotId) {
        GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(teamId, spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_ACTIVE));
        if (progress.getStatus() != ProgressStatus.ACTIVE) {
            throw new CustomException(ErrorCode.SPOT_NOT_ACTIVE);
        }
        return progress;
    }

    private GameProgress getActiveOrCompletedProgress(Long teamId, Long spotId) {
        GameProgress progress = gameProgressRepository.findByTeamIdAndCourseSpotId(teamId, spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_ACTIVE));
        if (progress.getStatus() == ProgressStatus.LOCKED) {
            throw new CustomException(ErrorCode.SPOT_NOT_ACTIVE);
        }
        return progress;
    }

    private CourseSpot getCourseSpotInCourse(Long spotId, Long courseId) {
        CourseSpot spot = courseSpotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_SPOT_NOT_FOUND));
        if (!spot.getCourse().getId().equals(courseId)) {
            throw new CustomException(ErrorCode.COURSE_SPOT_NOT_FOUND);
        }
        return spot;
    }

    private Mission getMission(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    private void validateMissionInCourse(Mission mission, Long courseId) {
        if (!mission.getCourseSpot().getCourse().getId().equals(courseId)) {
            throw new CustomException(ErrorCode.MISSION_NOT_FOUND);
        }
    }

    private void validateRunning(GameRoom room) {
        if (room.getStatus() != GameStatus.RUNNING) {
            throw new CustomException(ErrorCode.GAME_NOT_RUNNING);
        }
    }

    /** 팀원인지 확인하고 그 유저를 돌려준다. 기록에 남길 주체가 필요한 곳이 있어 검사와 조회를 합쳤다. */
    private User teamMemberUser(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndIsActiveTrue(teamId, userId)
                .map(TeamMember::getUser)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_TEAM_MEMBER));
    }

    private void validateRoomMember(Long roomId, Long userId) {
        if (!teamMemberRepository.existsByTeamGameRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private double haversineMeters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
