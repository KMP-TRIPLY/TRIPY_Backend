package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.game.dto.response.ActiveGameRoomResponse;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.ProgressStatus;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.GameProgressRepository;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.ranking.repository.RankingRepository;
import com.kmp.Triply.domain.reward.service.GameRewardGrantService;
import com.kmp.Triply.domain.reward.service.RewardService;
import com.kmp.Triply.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 방을 껐다 다시 찾아오는 길(진행 중인 방 조회)과, 아무도 끝내지 않은 방을 정리하는 길을 확인한다. */
class GameRoomLifecycleTest {

    private final GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
    private final GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
    private final MissionAttemptRepository missionAttemptRepository = mock(MissionAttemptRepository.class);
    private final RankingRepository rankingRepository = mock(RankingRepository.class);
    private final CourseSpotRepository courseSpotRepository = mock(CourseSpotRepository.class);
    private final GameRoomRealtimeNotifier notifier = mock(GameRoomRealtimeNotifier.class);
    private final GameRewardGrantService gameRewardGrantService = mock(GameRewardGrantService.class);
    private final RewardService rewardService = mock(RewardService.class);

    private GameRoomServiceImpl service() {
        GameRoomServiceImpl service = new GameRoomServiceImpl(
                gameRoomRepository, gameProgressRepository, teamRepository, teamMemberRepository, null,
                missionAttemptRepository, rankingRepository, null, courseSpotRepository, null, null,
                notifier, gameRewardGrantService, rewardService, null);
        ReflectionTestUtils.setField(service, "waitingExpireHours", 6L);
        ReflectionTestUtils.setField(service, "runningExpireHours", 24L);
        return service;
    }

    @Test
    void 아직_끝내지_않은_내_방을_진행도와_함께_돌려준다() {
        User me = user(1L);
        GameRoom room = room(10L, me);
        room.start();
        Team team = team(7L, room);
        TeamMember member = member(team, me);

        when(teamMemberRepository
                .findAllByUserIdAndIsActiveTrueAndTeamGameRoomStatusInOrderByTeamGameRoomCreatedAtDesc(
                        eq(1L), any()))
                .thenReturn(List.of(member));
        when(teamMemberRepository.countByTeamGameRoomIdAndIsActiveTrue(10L)).thenReturn(3L);
        when(courseSpotRepository.countByCourseId(20L)).thenReturn(4L);
        when(gameProgressRepository.countByTeamIdAndStatus(7L, ProgressStatus.COMPLETED)).thenReturn(2L);

        List<ActiveGameRoomResponse> rooms = service().getMyActiveRooms(1L);

        assertThat(rooms).hasSize(1);
        ActiveGameRoomResponse response = rooms.get(0);
        assertThat(response.getRoomId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(GameStatus.RUNNING);
        assertThat(response.getTotalSpots()).isEqualTo(4L);
        assertThat(response.getCompletedSpots()).isEqualTo(2L);
        assertThat(response.isHost()).isTrue();
    }

    @Test
    void 오래_시작되지_않은_대기실은_자동으로_취소된다() {
        GameRoom room = room(10L, user(1L));
        when(gameRoomRepository.findStaleWaitingRooms(any())).thenReturn(List.of(room));
        when(gameRoomRepository.findStaleRunningRooms(any())).thenReturn(List.of());

        service().expireStaleRooms();

        assertThat(room.getStatus()).isEqualTo(GameStatus.CANCELLED);
        verify(notifier).publish(eq(10L), eq("ROOM_AUTO_CANCELLED"), any(), any());
        // 시작도 안 한 방이라 정산할 점수가 없다.
        verify(rewardService, never()).settleFinishedRoom(anyLong());
    }

    @Test
    void 하루가_지나도록_끝나지_않은_방은_그때까지의_점수로_정산되고_종료된다() {
        GameRoom room = room(10L, user(1L));
        room.start();
        Team team = team(7L, room);

        when(gameRoomRepository.findStaleWaitingRooms(any())).thenReturn(List.of());
        when(gameRoomRepository.findStaleRunningRooms(any())).thenReturn(List.of(room));
        when(teamRepository.findTeamRankingRowsByGameRoomId(10L))
                .thenReturn(List.<Object[]>of(new Object[]{team, 900, 6, 1}));
        when(missionAttemptRepository.findPersonalFinalRankingRowsByGameRoomId(10L))
                .thenReturn(List.of());

        service().expireStaleRooms();

        assertThat(room.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(team.getRank()).isEqualTo((short) 1);
        verify(rankingRepository).saveAll(any());
        verify(gameRewardGrantService).grantForFinishedGame(room);
        verify(rewardService).settleFinishedRoom(10L);
        verify(notifier).publish(eq(10L), eq("ROOM_AUTO_FINISHED"), any(), any());
    }

    @Test
    void 정리_시간을_0으로_두면_아무_방도_건드리지_않는다() {
        GameRoomServiceImpl service = service();
        ReflectionTestUtils.setField(service, "waitingExpireHours", 0L);
        ReflectionTestUtils.setField(service, "runningExpireHours", 0L);

        service.expireStaleRooms();

        verify(gameRoomRepository, never()).findStaleWaitingRooms(any());
        verify(gameRoomRepository, never()).findStaleRunningRooms(any());
    }

    private static GameRoom room(Long id, User host) {
        Course course = Course.builder()
                .title("사비의 마지막 하루")
                .regionCode("44")
                .city("부여")
                .build();
        ReflectionTestUtils.setField(course, "id", 20L);

        GameRoom room = GameRoom.builder()
                .course(course)
                .host(host)
                .roomCode("ABC123")
                .maxMembers((short) 4)
                .build();
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now().minusDays(2));
        return room;
    }

    private static Team team(Long id, GameRoom room) {
        Team team = Team.builder().gameRoom(room).teamName("부여 원정대").build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private static TeamMember member(Team team, User user) {
        TeamMember member = TeamMember.builder().team(team).user(user).build();
        ReflectionTestUtils.setField(member, "id", 100L);
        ReflectionTestUtils.setField(member, "joinedAt", LocalDateTime.now().minusHours(3));
        return member;
    }

    private static User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@tripy.test")
                .nickname("user" + id)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
