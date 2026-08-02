package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.dto.response.MissionChoiceResponse;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.game.dto.request.HintRequest;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.request.SpotArriveRequest;
import com.kmp.Triply.domain.game.dto.response.HintResponse;
import com.kmp.Triply.domain.game.dto.response.MissionSubmitResponse;
import com.kmp.Triply.domain.game.dto.response.PlayChoiceResponse;
import com.kmp.Triply.domain.game.dto.response.PlayMissionResponse;
import com.kmp.Triply.domain.game.dto.response.SpotArriveResponse;
import com.kmp.Triply.domain.game.dto.response.SpotProgressResponse;
import com.kmp.Triply.domain.game.dto.response.TeamProgressResponse;
import com.kmp.Triply.domain.game.entity.AttemptResult;
import com.kmp.Triply.domain.game.entity.AttemptType;
import com.kmp.Triply.domain.game.entity.GameProgress;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.MissionAttempt;
import com.kmp.Triply.domain.game.entity.ProgressStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.repository.GameProgressRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamePlayServiceImpl implements GamePlayService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GameProgressRepository gameProgressRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final MissionRepository missionRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final GameRoomRealtimeNotifier realtimeNotifier;
    private final ObjectMapper objectMapper;

    @Override
    public TeamProgressResponse getTeamProgress(Long userId, Long roomId, Long teamId) {
        Team team = getTeamInRoom(teamId, roomId);
        validateRoomMember(roomId, userId);

        Long courseId = team.getGameRoom().getCourse().getId();
        List<CourseSpot> spots = courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(courseId);

        List<SpotProgressResponse> spotProgresses = spots.stream()
                .map(spot -> toSpotProgress(teamId, spot))
                .toList();

        return TeamProgressResponse.of(team, spotProgresses);
    }

    @Override
    @Transactional
    public SpotArriveResponse arriveSpot(Long userId, Long roomId, Long spotId, SpotArriveRequest request) {
        Team team = getTeamInRoom(request.getTeamId(), roomId);
        validateTeamMember(team.getId(), userId);
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

    @Override
    public List<PlayMissionResponse> getSpotMissions(Long userId, Long roomId, Long spotId, Long teamId) {
        Team team = getTeamInRoom(teamId, roomId);
        validateRoomMember(roomId, userId);

        CourseSpot spot = getCourseSpotInCourse(spotId, team.getGameRoom().getCourse().getId());
        GameProgress progress = getActiveOrCompletedProgress(teamId, spot.getId());

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

    @Override
    @Transactional
    public HintResponse requestHint(Long userId, Long missionId, HintRequest request) {
        Mission mission = getMission(missionId);
        Team team = team(request.getTeamId());
        validateTeamMember(team.getId(), userId);
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
                    .user(team.getLeader())
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

    @Override
    @Transactional
    public MissionSubmitResponse submitMission(Long userId, Long missionId, MissionSubmitRequest request) {
        Mission mission = getMission(missionId);
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        validateTeamMember(team.getId(), userId);
        GameRoom room = team.getGameRoom();
        validateRunning(room);
        validateMissionInCourse(mission, room.getCourse().getId());

        CourseSpot spot = mission.getCourseSpot();
        GameProgress progress = getActiveProgress(team.getId(), spot.getId());

        if (missionAttemptRepository.existsByGameProgressIdAndMissionIdAndResult(
                progress.getId(), missionId, AttemptResult.CORRECT)) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_SOLVED);
        }

        boolean hintUsed = missionAttemptRepository.existsByGameProgressIdAndMissionIdAndAttemptType(
                progress.getId(), missionId, AttemptType.HINT_REQUEST);
        boolean correct = grade(mission, request);
        AttemptResult result = correct ? AttemptResult.CORRECT : AttemptResult.WRONG;
        int scoreEarned = correct
                ? Math.max(0, mission.getBaseScore() - (hintUsed ? mission.getHintPenalty() : 0))
                : 0;

        User submitter = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .map(member -> member.getUser())
                .orElse(team.getLeader());

        missionAttemptRepository.save(MissionAttempt.builder()
                .gameProgress(progress)
                .mission(mission)
                .user(submitter)
                .attemptType(AttemptType.SUBMIT)
                .submittedValue(request.getSubmittedValue())
                .photoUrl(request.getPhotoUrl())
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
                        team.getTeamName() + " 팀이 스팟을 완료했습니다.", spot.getId());
            }
            realtimeNotifier.publish(room.getId(), "SCORE_UPDATED",
                    team.getTeamName() + " 팀 점수가 갱신되었습니다.", team.getTotalScore());
        }
        realtimeNotifier.publish(room.getId(), "MISSION_SOLVED",
                team.getTeamName() + " 팀 미션 결과: " + result.name(), missionId);

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

    private boolean grade(Mission mission, MissionSubmitRequest request) {
        return switch (mission.getMissionType()) {
            case QUIZ_TEXT -> matchesText(request.getSubmittedValue(), mission.getAnswer());
            case QUIZ_CHOICE -> matchesCorrectChoice(mission.getChoices(), request.getSubmittedValue());
            case PHOTO, NFC, AR, VOICE -> StringUtils.hasText(request.getPhotoUrl());
        };
    }

    private boolean matchesText(String submitted, String answer) {
        if (!StringUtils.hasText(submitted) || !StringUtils.hasText(answer)) {
            return false;
        }
        return submitted.trim().equalsIgnoreCase(answer.trim());
    }

    private boolean matchesCorrectChoice(String choicesJson, String submitted) {
        if (!StringUtils.hasText(submitted) || !StringUtils.hasText(choicesJson)) {
            return false;
        }
        String normalized = submitted.trim();
        return readChoices(choicesJson).stream()
                .filter(MissionChoiceResponse::isCorrect)
                .anyMatch(choice -> normalized.equalsIgnoreCase(choice.getValue())
                        || normalized.equalsIgnoreCase(choice.getLabel()));
    }

    private List<PlayChoiceResponse> toPlayChoices(String choicesJson) {
        if (!StringUtils.hasText(choicesJson)) {
            return Collections.emptyList();
        }
        return readChoices(choicesJson).stream()
                .map(choice -> PlayChoiceResponse.builder()
                        .label(choice.getLabel())
                        .value(choice.getValue())
                        .build())
                .toList();
    }

    private List<MissionChoiceResponse> readChoices(String choicesJson) {
        try {
            return objectMapper.readValue(
                    choicesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MissionChoiceResponse.class));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
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

    private Team getTeamInRoom(Long teamId, Long roomId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        if (roomId != null && !team.getGameRoom().getId().equals(roomId)) {
            throw new CustomException(ErrorCode.GAME_ROOM_ACCESS_DENIED);
        }
        return team;
    }

    private Team team(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
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

    private void validateTeamMember(Long teamId, Long userId) {
        if (teamMemberRepository.findByTeamIdAndUserId(teamId, userId).isEmpty()) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private void validateRoomMember(Long roomId, Long userId) {
        if (!teamMemberRepository.existsByTeamGameRoomIdAndUserId(roomId, userId)) {
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