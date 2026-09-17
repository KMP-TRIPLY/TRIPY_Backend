package com.kmp.Triply.domain.ranking.service;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.game.repository.MissionAttemptRepository;
import com.kmp.Triply.domain.ranking.dto.response.RankingEntryResponse;
import com.kmp.Triply.domain.ranking.dto.response.RankingResponse;
import com.kmp.Triply.domain.ranking.repository.RankingRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingLookupTest {

    @Mock
    private GameRoomRepository gameRoomRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private MissionAttemptRepository missionAttemptRepository;
    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private RankingServiceImpl service;

    @Test
    void 종료된_방도_멤버별_순위를_돌려준다() {
        GameRoom room = room();
        room.start();
        room.finish();

        when(gameRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(missionAttemptRepository.findPersonalLiveRankingsByGameRoomId(10L))
                .thenReturn(List.<Object[]>of(
                        new Object[]{2L, "동행", 900},
                        new Object[]{1L, "시니", 600}));

        RankingResponse response = service.getLiveRankings(10L);

        assertThat(response.getRankings()).extracting(RankingEntryResponse::getTargetName)
                .containsExactly("동행", "시니");
        assertThat(response.getRankings().get(0).getRank()).isEqualTo(1);
    }

    @Test
    void 진행_중인_방도_그대로_돌려준다() {
        GameRoom room = room();
        room.start();

        when(gameRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(missionAttemptRepository.findPersonalLiveRankingsByGameRoomId(10L))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "시니", 300}));

        assertThat(service.getLiveRankings(10L).getRankings()).hasSize(1);
    }

    @Test
    void 아직_시작하지_않은_방은_볼_점수가_없다() {
        when(gameRoomRepository.findById(10L)).thenReturn(Optional.of(room()));

        assertThatThrownBy(() -> service.getLiveRankings(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_GAME_ROOM_STATUS);
    }

    private static GameRoom room() {
        Course course = Course.builder()
                .title("사비의 마지막 하루")
                .regionCode("44")
                .city("부여")
                .build();
        ReflectionTestUtils.setField(course, "id", 3L);

        User host = User.builder().email("host@tripy.test").nickname("시니").build();
        ReflectionTestUtils.setField(host, "id", 1L);

        GameRoom room = GameRoom.builder()
                .course(course)
                .host(host)
                .roomCode("ABC123")
                .maxMembers((short) 4)
                .build();
        ReflectionTestUtils.setField(room, "id", 10L);
        return room;
    }
}
