package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomJoinRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomStartRequest;
import com.kmp.Triply.domain.game.dto.response.GameRoomJoinResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomResponse;
import com.kmp.Triply.domain.game.dto.response.TeamMemberResponse;
import com.kmp.Triply.domain.game.dto.response.TeamRankingResponse;
import com.kmp.Triply.domain.game.entity.GameMode;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.entity.TeamRole;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameRoomServiceImpl implements GameRoomService {

    private static final String ROOM_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ROOM_CODE_LENGTH = 6;

    private final GameRoomRepository gameRoomRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MissionAttemptRepository missionAttemptRepository;
    private final RankingRepository rankingRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserTravelProfileRepository userTravelProfileRepository;
    private final GameRoomRealtimeNotifier realtimeNotifier;
    private final SecureRandom secureRandom = new SecureRandom();

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
                .gameMode(GameMode.TEAM)
                .maxTeams(request.getMaxTeams())
                .build());

        Team team = createTeamWithMember(gameRoom, host, request.getTeamName(), TeamRole.LEADER);
        GameRoomJoinResponse response = GameRoomJoinResponse.of(gameRoom, team);
        realtimeNotifier.publish(gameRoom.getId(), "ROOM_CREATED", "게임 방이 생성되었습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public GameRoomJoinResponse joinRoom(Long userId, GameRoomJoinRequest request) {
        User user = getUser(userId);
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(normalizeRoomCode(request.getRoomCode()))
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));

        validateWaitingRoom(gameRoom);
        if (teamMemberRepository.existsByTeamGameRoomIdAndUserId(gameRoom.getId(), userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_ROOM);
        }

        Team team = resolveJoinTeam(gameRoom, user, request);
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamRole.MEMBER)
                .build();
        teamMemberRepository.save(teamMember);

        GameRoomJoinResponse response = GameRoomJoinResponse.of(gameRoom, team);
        realtimeNotifier.publish(gameRoom.getId(), "MEMBER_JOINED", "새 멤버가 게임 방에 참여했습니다.", response);
        return response;
    }

    @Override
    @Transactional
    public GameRoomResponse startRoom(Long userId, GameRoomStartRequest request) {
        GameRoom gameRoom = getRoom(request.getRoomId());
        validateHost(gameRoom, userId);
        validateWaitingRoom(gameRoom);

        gameRoom.start();
        teamRepository.findAllByGameRoomId(gameRoom.getId())
                .forEach(Team::startPlaying);

        GameRoomResponse response = GameRoomResponse.from(gameRoom);
        realtimeNotifier.publish(gameRoom.getId(), "ROOM_STARTED", "게임이 시작되었습니다.", response);
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
    public List<TeamRankingResponse> getRankings(Long roomId) {
        if (!gameRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND);
        }

        List<Object[]> teamRankingRows = teamRepository.findTeamRankingRowsByGameRoomId(roomId);
        return toRankings(teamRankingRows);
    }

    @Override
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        return teamMemberRepository.findAllByTeamId(teamId).stream()
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
        validateHost(gameRoom, hostUserId);
        validateWaitingRoom(gameRoom);
        if (hostUserId.equals(userId)) {
            throw new CustomException(ErrorCode.GAME_ROOM_ACCESS_DENIED);
        }

        TeamMember teamMember = teamMemberRepository.findByTeamGameRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        teamMemberRepository.delete(teamMember);
        realtimeNotifier.publish(roomId, "MEMBER_KICKED", "멤버가 게임 방에서 강퇴되었습니다.", userId);
    }

    private Team resolveJoinTeam(GameRoom gameRoom, User user, GameRoomJoinRequest request) {
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
            if (!team.getGameRoom().getId().equals(gameRoom.getId())) {
                throw new CustomException(ErrorCode.GAME_ROOM_ACCESS_DENIED);
            }
            return team;
        }

        if (teamRepository.countByGameRoomId(gameRoom.getId()) >= gameRoom.getMaxTeams()) {
            throw new CustomException(ErrorCode.ROOM_CAPACITY_EXCEEDED);
        }

        String teamName = StringUtils.hasText(request.getTeamName())
                ? request.getTeamName()
                : user.getNickname() + " 팀";
        return teamRepository.save(Team.builder()
                .gameRoom(gameRoom)
                .leader(user)
                .teamName(teamName)
                .build());
    }

    private Team createTeamWithMember(GameRoom gameRoom, User user, String teamName, TeamRole role) {
        Team team = teamRepository.save(Team.builder()
                .gameRoom(gameRoom)
                .leader(user)
                .teamName(teamName)
                .build());
        teamMemberRepository.save(TeamMember.builder()
                .team(team)
                .user(user)
                .role(role)
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

    private void validateWaitingRoom(GameRoom gameRoom) {
        if (gameRoom.getStatus() != GameStatus.WAITING) {
            throw new CustomException(ErrorCode.INVALID_GAME_ROOM_STATUS);
        }
    }

    private String normalizeRoomCode(String roomCode) {
        return roomCode.trim().toUpperCase(Locale.ROOT);
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

    private List<TeamRankingResponse> toRankings(List<Object[]> teamRankingRows) {
        return java.util.stream.IntStream.range(0, teamRankingRows.size())
                .mapToObj(index -> {
                    Object[] row = teamRankingRows.get(index);
                    return TeamRankingResponse.of(
                            (Team) row[0],
                            index + 1,
                            ((Number) row[1]).intValue(),
                            ((Number) row[3]).shortValue()
                    );
                })
                .toList();
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
                    .rankingType(RankingType.TEAM)
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
