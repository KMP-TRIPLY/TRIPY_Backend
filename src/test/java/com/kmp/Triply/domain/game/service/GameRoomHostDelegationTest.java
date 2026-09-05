package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameRoomHostDelegationTest {

    @Test
    void 정원이_찬_방이_시간안에_시작되지_않으면_입장순서상_다음_멤버가_방장이_된다() {
        GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        GameRoomRealtimeNotifier notifier = mock(GameRoomRealtimeNotifier.class);
        GameRoomServiceImpl service = new GameRoomServiceImpl(
                gameRoomRepository, null, teamMemberRepository, null, null, null,
                null, null, null, notifier, null);
        ReflectionTestUtils.setField(service, "hostDelegationTimeoutMinutes", 5L);

        User host = user(1L);
        User second = user(2L);
        User third = user(3L);
        GameRoom room = room(10L, host, (short) 3, LocalDateTime.now().minusMinutes(6));
        Team team = Team.builder().gameRoom(room).teamName("공주 원정대").build();

        when(gameRoomRepository.findHostDelegationCandidates(any()))
                .thenReturn(List.of(room));
        when(teamMemberRepository.findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(room.getId()))
                .thenReturn(List.of(
                        member(100L, team, host, LocalDateTime.now().minusMinutes(10)),
                        member(101L, team, second, LocalDateTime.now().minusMinutes(9)),
                        member(102L, team, third, LocalDateTime.now().minusMinutes(8))
                ));

        service.delegateTimedOutHosts();

        assertThat(room.getHost()).isSameAs(second);
        assertThat(room.getReadySinceAt()).isAfter(LocalDateTime.now().minusMinutes(1));
        verify(notifier).publish(eq(room.getId()), eq("HOST_DELEGATED"),
                eq("게임 시작 지연으로 방장 권한이 위임되었습니다."), any());
    }

    @Test
    void 정원이_차지_않았으면_방장_위임_타이머를_초기화한다() {
        GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        GameRoomServiceImpl service = new GameRoomServiceImpl(
                gameRoomRepository, null, teamMemberRepository, null, null, null,
                null, null, null, mock(GameRoomRealtimeNotifier.class), null);
        ReflectionTestUtils.setField(service, "hostDelegationTimeoutMinutes", 5L);

        User host = user(1L);
        User second = user(2L);
        GameRoom room = room(10L, host, (short) 3, LocalDateTime.now().minusMinutes(6));
        Team team = Team.builder().gameRoom(room).teamName("공주 원정대").build();

        when(gameRoomRepository.findHostDelegationCandidates(any()))
                .thenReturn(List.of(room));
        when(teamMemberRepository.findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(room.getId()))
                .thenReturn(List.of(
                        member(100L, team, host, LocalDateTime.now().minusMinutes(10)),
                        member(101L, team, second, LocalDateTime.now().minusMinutes(9))
                ));

        service.delegateTimedOutHosts();

        assertThat(room.getHost()).isSameAs(host);
        assertThat(room.getReadySinceAt()).isNull();
    }

    @Test
    void 대기중_방장이_나가면_입장순서상_다음_멤버가_방장이_된다() {
        GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
        TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
        GameRoomRealtimeNotifier notifier = mock(GameRoomRealtimeNotifier.class);
        GameRoomServiceImpl service = new GameRoomServiceImpl(
                gameRoomRepository, null, teamMemberRepository, null, null, null,
                null, null, null, notifier, null);

        User host = user(1L);
        User second = user(2L);
        User third = user(3L);
        GameRoom room = room(10L, host, (short) 3, LocalDateTime.now());
        Team team = Team.builder().gameRoom(room).teamName("공주 원정대").build();
        TeamMember hostMember = member(100L, team, host, LocalDateTime.now().minusMinutes(10));
        TeamMember secondMember = member(101L, team, second, LocalDateTime.now().minusMinutes(9));
        TeamMember thirdMember = member(102L, team, third, LocalDateTime.now().minusMinutes(8));

        when(gameRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(teamMemberRepository.findByTeamGameRoomIdAndUserIdAndIsActiveTrue(room.getId(), host.getId()))
                .thenReturn(Optional.of(hostMember));
        when(teamMemberRepository.findAllByTeamGameRoomIdAndIsActiveTrueOrderByJoinedAtAscIdAsc(room.getId()))
                .thenReturn(List.of(hostMember, secondMember, thirdMember));

        service.leaveRoom(host.getId(), room.getId(), null);

        assertThat(room.getHost()).isSameAs(second);
        assertThat(room.getReadySinceAt()).isNull();
        verify(teamMemberRepository).delete(hostMember);
        verify(notifier).publish(eq(room.getId()), eq("HOST_DELEGATED"),
                eq("방장이 나가 방장 권한이 위임되었습니다."), any());
        verify(notifier).publish(eq(room.getId()), eq("MEMBER_LEFT"),
                eq("멤버가 대기실에서 나갔습니다."), any());
    }

    private static GameRoom room(Long id, User host, short maxMembers, LocalDateTime readySinceAt) {
        Course course = Course.builder()
                .title("코스")
                .regionCode("44")
                .city("공주")
                .build();
        ReflectionTestUtils.setField(course, "id", 20L);

        GameRoom room = GameRoom.builder()
                .course(course)
                .host(host)
                .roomCode("ABC123")
                .maxMembers(maxMembers)
                .build();
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "readySinceAt", readySinceAt);
        return room;
    }

    private static TeamMember member(Long id, Team team, User user, LocalDateTime joinedAt) {
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "joinedAt", joinedAt);
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
