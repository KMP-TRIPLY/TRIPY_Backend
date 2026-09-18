package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.response.SpotCompletedEventResponse;
import com.kmp.Triply.domain.game.entity.AttemptResult;
import com.kmp.Triply.domain.game.entity.AttemptType;
import com.kmp.Triply.domain.game.entity.GameProgress;
import com.kmp.Triply.domain.game.entity.GameRoom;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 스팟을 끝냈을 때 팀원 전체가 다음 스팟을 알 수 있어야 한다.
 * 예전에는 완료된 스팟 번호만 실려 나가, 마지막 미션을 낸 사람만 다음 스팟을 알았다.
 */
@ExtendWith(MockitoExtension.class)
class SpotCompletedEventTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private GameProgressRepository gameProgressRepository;
    @Mock
    private MissionAttemptRepository missionAttemptRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private CourseSpotRepository courseSpotRepository;
    @Mock
    private GameRoomRealtimeNotifier realtimeNotifier;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private PhotoStorageService photoStorage;
    @Mock
    private PhotoVerificationService photoVerifier;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GamePlayService service;

    private final Course course = course();
    private final CourseSpot spot = spot(29L, (short) 1);
    private final CourseSpot nextSpot = spot(30L, (short) 2);
    private final Mission mission = mission(60L, spot);

    @Test
    void 스팟의_마지막_미션을_풀면_다음_스팟까지_실어_알린다() {
        stubSubmitFlow();
        when(courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(3L))
                .thenReturn(List.of(spot, nextSpot));

        service.submitMission(19L, 60L, request("정답"));

        SpotCompletedEventResponse payload = capturedSpotCompletedPayload();
        assertThat(payload.getSpotId()).isEqualTo(29L);
        assertThat(payload.getNextSpotId()).isEqualTo(30L);
        assertThat(payload.isCourseCompleted()).isFalse();
    }

    @Test
    void 코스의_마지막_스팟이면_다음_스팟_없이_완주로_알린다() {
        stubSubmitFlow();
        when(courseSpotRepository.findAllByCourseIdOrderBySequenceOrderAsc(3L))
                .thenReturn(List.of(spot));

        service.submitMission(19L, 60L, request("정답"));

        SpotCompletedEventResponse payload = capturedSpotCompletedPayload();
        assertThat(payload.getNextSpotId()).isNull();
        assertThat(payload.isCourseCompleted()).isTrue();
    }

    /** 스팟에 미션이 2개이고 이미 1개를 풀어 둔 상태에서, 나머지 하나를 맞히는 상황. */
    private void stubSubmitFlow() {
        GameRoom room = room();
        Team team = team(room);
        GameProgress progress = progress(team);

        when(missionRepository.findById(60L)).thenReturn(Optional.of(mission));
        when(teamRepository.findOfRoom(66L)).thenReturn(team);
        when(teamMemberRepository.findByTeamIdAndUserIdAndIsActiveTrue(7L, 19L))
                .thenReturn(Optional.of(member(team, user())));
        when(gameProgressRepository.findByTeamIdAndCourseSpotId(7L, 29L)).thenReturn(Optional.of(progress));
        when(missionAttemptRepository.existsByGameProgressIdAndMissionIdAndResult(
                5L, 60L, AttemptResult.CORRECT)).thenReturn(false);
        when(missionAttemptRepository.existsByGameProgressIdAndMissionIdAndAttemptType(
                5L, 60L, AttemptType.HINT_REQUEST)).thenReturn(false);
        when(missionAttemptRepository.save(any(MissionAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(missionAttemptRepository.countByGameProgressIdAndResult(5L, AttemptResult.CORRECT))
                .thenReturn(2L);
        when(missionRepository.findAllByCourseSpotIdOrderByIdAsc(29L))
                .thenReturn(List.of(mission, mission(61L, spot)));
    }

    private SpotCompletedEventResponse capturedSpotCompletedPayload() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(realtimeNotifier, atLeastOnce())
                .publish(anyLong(), eq("SPOT_COMPLETED"), any(), payloadCaptor.capture());
        return (SpotCompletedEventResponse) payloadCaptor.getValue();
    }

    private static MissionSubmitRequest request(String submittedValue) {
        MissionSubmitRequest request = new MissionSubmitRequest();
        ReflectionTestUtils.setField(request, "roomId", 66L);
        ReflectionTestUtils.setField(request, "submittedValue", submittedValue);
        return request;
    }

    private static Course course() {
        Course course = Course.builder().title("사비의 마지막 하루").regionCode("44").city("부여").build();
        ReflectionTestUtils.setField(course, "id", 3L);
        return course;
    }

    private CourseSpot spot(Long id, short sequenceOrder) {
        CourseSpot spot = CourseSpot.builder()
                .course(course)
                .sequenceOrder(sequenceOrder)
                .lat(BigDecimal.valueOf(36.287))
                .lng(BigDecimal.valueOf(126.9128))
                .radiusMeters(120)
                .build();
        ReflectionTestUtils.setField(spot, "id", id);
        return spot;
    }

    private static Mission mission(Long id, CourseSpot spot) {
        Mission mission = Mission.builder()
                .courseSpot(spot)
                .missionType(MissionType.QUIZ_TEXT)
                .question("고란사 뒤 바위틈에서 자라는 식물은?")
                .answer("정답")
                .hintPenalty(150)
                .baseScore(300)
                .build();
        ReflectionTestUtils.setField(mission, "id", id);
        return mission;
    }

    private GameRoom room() {
        GameRoom room = GameRoom.builder()
                .course(course)
                .host(user())
                .roomCode("ABC123")
                .maxMembers((short) 4)
                .build();
        ReflectionTestUtils.setField(room, "id", 66L);
        room.start();
        return room;
    }

    private static Team team(GameRoom room) {
        Team team = Team.builder().gameRoom(room).teamName("커피").build();
        ReflectionTestUtils.setField(team, "id", 7L);
        return team;
    }

    private GameProgress progress(Team team) {
        GameProgress progress = GameProgress.builder()
                .team(team)
                .courseSpot(spot)
                .status(ProgressStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(progress, "id", 5L);
        return progress;
    }

    private static TeamMember member(Team team, User user) {
        TeamMember member = TeamMember.builder().team(team).user(user).build();
        ReflectionTestUtils.setField(member, "id", 100L);
        return member;
    }

    private static User user() {
        User user = User.builder().email("kim@tripy.test").nickname("김유진").build();
        ReflectionTestUtils.setField(user, "id", 19L);
        return user;
    }
}
