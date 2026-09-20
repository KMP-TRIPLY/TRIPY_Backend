package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.TeamMemberRepository;
import com.kmp.Triply.domain.game.repository.TeamRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 게임을 뛰는 중에 새 방을 만들면 팀이 하나 더 생겨 안 끝난 방이 둘이 된다.
 * 대기 중인 방은 언제든 나갈 수 있으므로 막지 않는다.
 */
class GameRoomCreateGuardTest {

    private final GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final GameRoomRealtimeNotifier notifier = mock(GameRoomRealtimeNotifier.class);

    private final User host = mock(User.class);

    private GameRoomService service() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        return new GameRoomService(
                gameRoomRepository, null, teamRepository, teamMemberRepository, null,
                null, null, courseRepository, null, userRepository, null,
                notifier, null, null, null);
    }

    @Test
    void 진행_중인_게임이_있으면_새_방을_못_만든다() {
        when(teamMemberRepository.existsByUserIdAndIsActiveTrueAndTeamGameRoomStatus(1L, GameStatus.RUNNING))
                .thenReturn(true);

        assertThatThrownBy(() -> service().createRoom(1L, request()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_PLAYING);

        verify(gameRoomRepository, never()).save(any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void 대기_중인_방만_있으면_막지_않는다() {
        when(teamMemberRepository.existsByUserIdAndIsActiveTrueAndTeamGameRoomStatus(1L, GameStatus.RUNNING))
                .thenReturn(false);
        // 가드를 지나 코스 조회까지 갔다는 것으로 통과를 확인한다. 방 생성 전체를 흉내내지 않는다.
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().createRoom(1L, request()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    private GameRoomCreateRequest request() {
        GameRoomCreateRequest request = new GameRoomCreateRequest();
        ReflectionTestUtils.setField(request, "courseId", 9L);
        ReflectionTestUtils.setField(request, "roomName", "테스트 방");
        ReflectionTestUtils.setField(request, "maxMembers", (short) 4);
        return request;
    }
}
