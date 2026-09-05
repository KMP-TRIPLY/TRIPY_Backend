package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.dto.request.GameRoomCourseChangeRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomJoinRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomMaxMembersChangeRequest;

import com.kmp.Triply.domain.game.dto.request.GameRoomStartRequest;
import com.kmp.Triply.domain.game.dto.request.TeamLeaveRequest;
import com.kmp.Triply.domain.game.dto.response.GameRoomJoinResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomSummaryResponse;
import com.kmp.Triply.domain.game.dto.response.TeamLeaveResponse;
import com.kmp.Triply.domain.game.dto.response.TeamMemberResponse;
import com.kmp.Triply.domain.game.entity.GameMode;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamLeaveHistory;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamLeaveHistoryRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.ranking.entity.Ranking;
import com.kmp.Triply.domain.ranking.entity.RankingType;
import com.kmp.Triply.domain.ranking.repository.RankingRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.domain.user.repository.UserTravelProfileRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameRoomServiceImpl implements GameRoomService {

    private static final String ROOM_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ROOM_CODE_LENGTH = 6;

    private final GameRoomRepository gameRoomRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamLeaveHistoryRepository teamLeaveHistoryRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final RankingRepository rankingRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserTravelProfileRepository userTravelProfileRepository;
    private final GameRoomRealtimeNotifier realtimeNotifier;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${game.room.host-delegation-timeout-minutes:5}")
    private long hostDelegationTimeoutMinutes;

    @Override
    @Transactional
    public GameRoomJoinResponse createRoom(Long userId, GameRoomCreateRequest request) {
        User host = getUser(userId);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        GameRoom gameRoom = gameRoomRepository.save(GameRoom.builder()
                .course(course)
                .host(host)
                .roomCode(generateRoomCode())
                // 비밀번호는 선택이다. 넣으면 아는 사람만 들어올 수 있고, 비우면 누구나 들어온다.
                .passwordHash(StringUtils.hasText(request.getPassword())
                        ? passwordEncoder.encode(request.getPassword()) : null)
                // 정원이 1 이면 혼자 하는 방이다. 모드를 따로 받지 않고 정원에서 유도한다 —
                // 둘이 어긋나면(정원 1 인데 TEAM) 어느 쪽이 맞는지 알 수 없다.
                .gameMode(request.getMaxMembers() == 1 ? GameMode.SOLO : GameMode.TEAM)
                .maxMembers(request.getMaxMembers())
                .build());

        Team team = createTeamWithMember(gameRoom, host, request.getRoomName());
        refreshReadyState(gameRoom);
        GameRoomJoinResponse response = GameRoomJoinResponse.of(gameRoom, team);
        realtimeNotifier.publish(gameRoom.getId(), "ROOM_CREATED", "게임 방이 생성되었습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public GameRoomJoinResponse joinRoom(Long userId, Long roomId, GameRoomJoinRequest request) {
        User user = getUser(userId);
        GameRoom gameRoom = getRoom(roomId);

        // 이미 이 방의 멤버라면 새로 넣지 않고 원래 팀으로 돌려보낸다.
        // 앱을 껐다 켜거나 네트워크가 끊겨 다시 들어오는 경우가 정상 흐름이므로 실패로 처리하면 안 된다.
        // 진행 중(RUNNING)인 방도 재입장은 허용한다 — 막으면 게임 도중 튕긴 사람이 복귀할 수 없다.
        //
        // 재입장에는 비밀번호를 묻지 않는다. 이미 멤버인 것이 곧 통과 증명이고,
        // 앱이 꺼진 뒤엔 비밀번호를 들고 있지 않을 수 있어 물으면 복귀가 막힌다.
        var existing = teamMemberRepository.findByTeamGameRoomIdAndUserId(gameRoom.getId(), userId);
        if (existing.isPresent()) {
            return rejoinRoom(gameRoom, existing.get());
        }

        validatePassword(gameRoom, request);

        // 새로 들어오는 사람만 대기 중인 방으로 제한한다.
        validateWaitingRoom(gameRoom);
        if (teamMemberRepository.countByTeamGameRoomIdAndIsActiveTrue(gameRoom.getId()) >= gameRoom.getMaxMembers()) {
            throw new CustomException(ErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        Team team = teamOfRoom(gameRoom.getId());
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        teamMemberRepository.save(teamMember);
        refreshReadyState(gameRoom);

        GameRoomJoinResponse response = GameRoomJoinResponse.of(gameRoom, team);
        realtimeNotifier.publish(gameRoom.getId(), "MEMBER_JOINED", "새 멤버가 게임 방에 참여했습니다.", response);
        return response;
    }

    /** 참여할 방 고르기. 대기 중인 방만 보여준다 — 시작한 방에는 새로 들어갈 수 없다. */
    @Override
    public List<GameRoomSummaryResponse> getWaitingRooms() {
        return gameRoomRepository.findRoomSummariesByStatus(GameStatus.WAITING).stream()
                .map(row -> GameRoomSummaryResponse.of(
                        (GameRoom) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }

    @Override
    @Transactional
    public GameRoomResponse startRoom(Long userId, GameRoomStartRequest request) {
        GameRoom gameRoom = getRoom(request.getRoomId());
        delegateHostIfTimedOut(gameRoom, LocalDateTime.now());
        validateHost(gameRoom, userId);
        validateWaitingRoom(gameRoom);

        gameRoom.clearReady();
        gameRoom.start();
        teamRepository.findAllByGameRoomId(gameRoom.getId())
                .forEach(Team::startPlaying);

        GameRoomResponse response = GameRoomResponse.from(gameRoom);
        realtimeNotifier.publish(gameRoom.getId(), "ROOM_STARTED", "게임이 시작되었습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public GameRoomResponse changeCourse(Long userId, Long roomId, GameRoomCourseChangeRequest request) {
        GameRoom gameRoom = getRoom(roomId);
        delegateHostIfTimedOut(gameRoom, LocalDateTime.now());
        validateHost(gameRoom, userId);
        validateWaitingRoom(gameRoom);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        gameRoom.changeCourse(course);
        GameRoomResponse response = GameRoomResponse.from(gameRoom);
        realtimeNotifier.publish(gameRoom.getId(), "COURSE_CHANGED", "게임 방 코스가 변경되었습니다.", response);
        return response;
    }

    /**
     * 준비 중 정원 변경. "한 명 더 온다" 가 흔해서 방을 다시 만들지 않게 열어둔다.
     * 비밀번호는 바꿀 수 없다 — 이미 초대 링크를 받은 사람들이 못 들어오게 되고
     * 방장이 다시 다 알려줘야 해서 얻는 것 없이 혼란만 생긴다.
     */
    @Override
    @Transactional
    public GameRoomResponse changeMaxMembers(Long userId, Long roomId,
                                             GameRoomMaxMembersChangeRequest request) {
        GameRoom gameRoom = getRoom(roomId);
        delegateHostIfTimedOut(gameRoom, LocalDateTime.now());
        validateHost(gameRoom, userId);
        validateWaitingRoom(gameRoom);

        // 이미 들어온 사람을 밀어낼 수는 없으므로 현재 인원 아래로는 못 줄인다.
        long memberCount = teamMemberRepository.countByTeamGameRoomIdAndIsActiveTrue(roomId);
        if (request.getMaxMembers() < memberCount) {
            throw new CustomException(ErrorCode.ROOM_CAPACITY_BELOW_MEMBERS);
        }

        gameRoom.changeMaxMembers(request.getMaxMembers());
        refreshReadyState(gameRoom);
        GameRoomResponse response = GameRoomResponse.from(gameRoom);
        realtimeNotifier.publish(roomId, "ROOM_CAPACITY_CHANGED", "게임 방 정원이 변경되었습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public TeamLeaveResponse leaveRoom(Long userId, Long roomId, TeamLeaveRequest request) {
        GameRoom gameRoom = getRoom(roomId);
        TeamMember teamMember = teamMemberRepository.findByTeamGameRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        return switch (gameRoom.getStatus()) {
            case WAITING -> leaveWaitingRoom(gameRoom, teamMember);
            case RUNNING -> leaveRunningRoom(gameRoom, teamMember, request, userId);
            default -> throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        };
    }

    /**
     * 대기실 하차. 아직 시작 전이므로 기록을 남기지 않고 멤버 행 자체를 지운다.
     * 하차 이력이 없으니 마음이 바뀌면 다시 들어올 수 있다 — 시작 전에는 그게 자연스럽다.
     */
    private TeamLeaveResponse leaveWaitingRoom(GameRoom gameRoom, TeamMember teamMember) {
        Long userId = teamMember.getUser().getId();
        List<TeamMember> activeMembers = teamMemberRepository
                .findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(gameRoom.getId());
        delegateHostBeforeLeavingIfNeeded(gameRoom, teamMember, activeMembers);
        teamMemberRepository.delete(teamMember);
        gameRoom.clearReady();

        TeamLeaveResponse response = TeamLeaveResponse.ofWaitingRoom(gameRoom.getId(), userId);
        realtimeNotifier.publish(gameRoom.getId(), "MEMBER_LEFT", "멤버가 대기실에서 나갔습니다.", response);
        if (remainingMemberCount(activeMembers, userId) == 0) {
            gameRoom.cancel();
            realtimeNotifier.publish(gameRoom.getId(), "ROOM_CANCELLED",
                    "모든 멤버가 나가 게임 방이 취소되었습니다.", GameRoomResponse.from(gameRoom));
        }
        return response;
    }

    /** 진행 중 하차. 사유를 남기고 그때까지 낸 점수는 팀에 남긴다. 이 방에는 다시 못 들어온다. */
    private TeamLeaveResponse leaveRunningRoom(GameRoom gameRoom, TeamMember teamMember,
                                               TeamLeaveRequest request, Long userId) {
        int preservedScore = missionAttemptRepository.sumScoreByTeamIdAndUserId(
                teamMember.getTeam().getId(),
                userId
        );
        teamMember.leave();
        TeamLeaveHistory history = teamLeaveHistoryRepository.save(TeamLeaveHistory.builder()
                .gameRoom(gameRoom)
                .team(teamMember.getTeam())
                .user(teamMember.getUser())
                .reasonType(request.getReasonType())
                .reasonDetail(request.getReasonDetail())
                .preservedScore(preservedScore)
                .build());

        TeamLeaveResponse response = TeamLeaveResponse.from(history);
        realtimeNotifier.publish(gameRoom.getId(), "MEMBER_LEFT", "멤버가 게임 방에서 하차했습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public GameRoomResponse endRoom(Long userId, Long roomId) {
        GameRoom gameRoom = getRoom(roomId);
        validateHost(gameRoom, userId);
        if (gameRoom.getStatus() != GameStatus.RUNNING) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }

        List<Object[]> teamRankingRows = teamRepository.findTeamRankingRowsByGameRoomId(roomId);
        for (int index = 0; index < teamRankingRows.size(); index++) {
            Team team = (Team) teamRankingRows.get(index)[0];
            team.finish((short) (index + 1));
        }
        gameRoom.finish();
        saveFinalRankings(gameRoom, teamRankingRows);

        GameRoomResponse response = GameRoomResponse.from(gameRoom);
        realtimeNotifier.publish(gameRoom.getId(), "ROOM_FINISHED", "게임이 종료되고 점수가 잠겼습니다.", response);
        return response;
    }

    @Override
    public List<TeamMemberResponse> getRoomMembers(Long roomId) {
        return teamMemberRepository.findAllByTeamIdAndIsActiveTrue(teamOfRoom(roomId).getId()).stream()
                .map(teamMember -> TeamMemberResponse.from(
                        teamMember,
                        userTravelProfileRepository.findByUserId(teamMember.getUser().getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public void kickMember(Long hostUserId, Long roomId, Long userId) {
        GameRoom gameRoom = getRoom(roomId);
        delegateHostIfTimedOut(gameRoom, LocalDateTime.now());
        validateHost(gameRoom, hostUserId);
        validateWaitingRoom(gameRoom);
        if (hostUserId.equals(userId)) {
            throw new CustomException(ErrorCode.GAME_ROOM_ACCESS_DENIED);
        }

        TeamMember teamMember = teamMemberRepository.findByTeamGameRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        teamMemberRepository.delete(teamMember);
        gameRoom.clearReady();
        realtimeNotifier.publish(roomId, "MEMBER_KICKED", "멤버가 게임 방에서 강퇴되었습니다.", userId);
    }

    /**
     * 재입장. 요청에 teamId 가 있어도 무시하고 원래 팀으로 돌려보낸다 —
     * 재입장을 빌미로 팀을 갈아타면 점수가 따라다니게 된다.
     */
    private GameRoomJoinResponse rejoinRoom(GameRoom gameRoom, TeamMember member) {
        if (gameRoom.getStatus() == GameStatus.FINISHED) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }
        // 스스로 하차한 사람은 못 돌아온다. 끊겨서 다시 붙는 것과 구분하는 기준이 하차 이력이다
        // (튕긴 경우는 클라이언트가 아무 API 도 부르지 않으므로 이력이 남지 않는다).
        if (teamLeaveHistoryRepository.existsByGameRoomIdAndUserId(
                gameRoom.getId(), member.getUser().getId())) {
            throw new CustomException(ErrorCode.LEFT_ROOM_CANNOT_REJOIN);
        }
        if (!member.isActive()) {
            member.rejoin();
        }
        GameRoomJoinResponse response = GameRoomJoinResponse.of(gameRoom, member.getTeam());
        realtimeNotifier.publish(gameRoom.getId(), "MEMBER_REJOINED", "멤버가 다시 참여했습니다.", response);
        return response;
    }

    /**
     * 방 하나에 팀 하나. 방을 만들 때 같이 만들어지므로 항상 존재한다.
     * ponytail: teams 테이블을 GameRoom 에 흡수하지 않고 1:1 로 남겨둔 상태 —
     * 점수·진행상황 컬럼과 reward·ranking 도메인의 FK 를 옮기는 마이그레이션이 필요해 별건으로 미뤘다.
     */
    private Team teamOfRoom(Long roomId) {
        return teamRepository.findFirstByGameRoomIdOrderByIdAsc(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private Team createTeamWithMember(GameRoom gameRoom, User user, String teamName) {
        Team team = teamRepository.save(Team.builder()
                .gameRoom(gameRoom)
                .teamName(teamName)
                .build());
        teamMemberRepository.save(TeamMember.builder()
                .team(team)
                .user(user)
                .build());
        return team;
    }

    private GameRoom getRoom(Long roomId) {
        return gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateHost(GameRoom gameRoom, Long userId) {
        if (!gameRoom.getHost().getId().equals(userId)) {
            throw new CustomException(ErrorCode.GAME_ROOM_ACCESS_DENIED);
        }
    }

    @Scheduled(fixedDelayString = "${game.room.host-delegation-check-delay-ms:60000}")
    @Transactional
    public void delegateTimedOutHosts() {
        if (hostDelegationTimeoutMinutes <= 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(hostDelegationTimeoutMinutes);
        gameRoomRepository.findHostDelegationCandidates(cutoff)
                .forEach(gameRoom -> delegateHostIfReady(gameRoom, now));
    }

    private void refreshReadyState(GameRoom gameRoom) {
        if (gameRoom.getStatus() != GameStatus.WAITING || gameRoom.getMaxMembers() <= 1) {
            gameRoom.clearReady();
            return;
        }
        long activeMemberCount = teamMemberRepository.countByTeamGameRoomIdAndIsActiveTrue(gameRoom.getId());
        if (activeMemberCount >= gameRoom.getMaxMembers()) {
            gameRoom.markReady(LocalDateTime.now());
        } else {
            gameRoom.clearReady();
        }
    }

    private void delegateHostIfTimedOut(GameRoom gameRoom, LocalDateTime now) {
        if (hostDelegationTimeoutMinutes <= 0
                || gameRoom.getStatus() != GameStatus.WAITING
                || gameRoom.getReadySinceAt() == null
                || gameRoom.getReadySinceAt().plusMinutes(hostDelegationTimeoutMinutes).isAfter(now)) {
            return;
        }
        delegateHostIfReady(gameRoom, now);
    }

    private void delegateHostIfReady(GameRoom gameRoom, LocalDateTime delegatedAt) {
        List<TeamMember> members = teamMemberRepository
                .findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(gameRoom.getId());
        if (gameRoom.getStatus() != GameStatus.WAITING
                || gameRoom.getMaxMembers() <= 1
                || members.size() < gameRoom.getMaxMembers()) {
            gameRoom.clearReady();
            return;
        }

        nextHostByJoinOrder(gameRoom, members).ifPresent(nextHost -> {
            gameRoom.changeHost(nextHost, delegatedAt);
            GameRoomResponse response = GameRoomResponse.from(gameRoom);
            realtimeNotifier.publish(gameRoom.getId(), "HOST_DELEGATED",
                    "게임 시작 지연으로 방장 권한이 위임되었습니다.", response);
        });
    }

    private void delegateHostBeforeLeavingIfNeeded(GameRoom gameRoom, TeamMember leavingMember,
                                                  List<TeamMember> activeMembers) {
        if (!gameRoom.getHost().getId().equals(leavingMember.getUser().getId())) {
            return;
        }
        activeMembers.stream()
                .filter(member -> !member.getUser().getId().equals(leavingMember.getUser().getId()))
                .map(TeamMember::getUser)
                .findFirst()
                .ifPresent(nextHost -> {
                    gameRoom.changeHost(nextHost, LocalDateTime.now());
                    GameRoomResponse response = GameRoomResponse.from(gameRoom);
                    realtimeNotifier.publish(gameRoom.getId(), "HOST_DELEGATED",
                            "방장이 나가 방장 권한이 위임되었습니다.", response);
                });
    }

    private long remainingMemberCount(List<TeamMember> activeMembers, Long leavingUserId) {
        return activeMembers.stream()
                .filter(member -> !member.getUser().getId().equals(leavingUserId))
                .count();
    }

    private Optional<User> nextHostByJoinOrder(GameRoom gameRoom, List<TeamMember> members) {
        Long currentHostId = gameRoom.getHost().getId();
        return members.stream()
                .filter(member -> member.getUser().getId().equals(currentHostId))
                .findFirst()
                .flatMap(currentHost -> members.stream()
                        .filter(member -> joinedAfter(member, currentHost))
                        .map(TeamMember::getUser)
                        .findFirst())
                .or(() -> members.stream()
                        .map(TeamMember::getUser)
                        .filter(user -> !user.getId().equals(currentHostId))
                        .findFirst());
    }

    private boolean joinedAfter(TeamMember member, TeamMember currentHost) {
        int joinedAtOrder = member.getJoinedAt().compareTo(currentHost.getJoinedAt());
        return joinedAtOrder > 0
                || joinedAtOrder == 0 && member.getId() != null && currentHost.getId() != null
                && member.getId() > currentHost.getId();
    }

    /**
     * 잠긴 방에만 비밀번호를 요구한다. 비밀번호 없이 만든 방은 누구나 들어온다.
     * 초대 링크를 받았더라도 잠긴 방이면 비밀번호를 알아야 한다.
     */
    private void validatePassword(GameRoom gameRoom, GameRoomJoinRequest request) {
        if (!gameRoom.isLocked()) {
            return;
        }
        String password = request == null ? null : request.getPassword();
        if (!StringUtils.hasText(password)
                || !passwordEncoder.matches(password, gameRoom.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_PASSWORD);
        }
    }

    private void validateWaitingRoom(GameRoom gameRoom) {
        if (gameRoom.getStatus() != GameStatus.WAITING) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }
    }

    private String generateRoomCode() {
        String roomCode;
        do {
            StringBuilder builder = new StringBuilder(ROOM_CODE_LENGTH);
            for (int index = 0; index < ROOM_CODE_LENGTH; index++) {
                builder.append(ROOM_CODE_CHARACTERS.charAt(secureRandom.nextInt(ROOM_CODE_CHARACTERS.length())));
            }
            roomCode = builder.toString();
        } while (gameRoomRepository.existsByRoomCode(roomCode));
        return roomCode;
    }

    private void saveFinalRankings(GameRoom gameRoom, List<Object[]> teamRankingRows) {
        rankingRepository.deleteByGameRoomId(gameRoom.getId());

        List<Ranking> rankings = new ArrayList<>();
        for (int index = 0; index < teamRankingRows.size(); index++) {
            Object[] row = teamRankingRows.get(index);
            Team team = (Team) row[0];
            rankings.add(Ranking.builder()
                    .gameRoom(gameRoom)
                    .team(team)
                    .rankingType(RankingType.ROOM)
                    .rank((short) (index + 1))
                    .finalScore(((Number) row[1]).intValue())
                    .missionClearCount(((Number) row[2]).shortValue())
                    .hintUsedCount(((Number) row[3]).shortValue())
                    .build());
        }

        List<Object[]> personalRankingRows = missionAttemptRepository.findPersonalFinalRankingRowsByGameRoomId(gameRoom.getId());
        for (int index = 0; index < personalRankingRows.size(); index++) {
            Object[] row = personalRankingRows.get(index);
            User user = userRepository.getReferenceById((Long) row[0]);
            rankings.add(Ranking.builder()
                    .gameRoom(gameRoom)
                    .user(user)
                    .rankingType(RankingType.PERSONAL)
                    .rank((short) (index + 1))
                    .finalScore(((Number) row[2]).intValue())
                    .missionClearCount(((Number) row[3]).shortValue())
                    .hintUsedCount(((Number) row[4]).shortValue())
                    .build());
        }

        rankingRepository.saveAll(rankings);
    }
}
