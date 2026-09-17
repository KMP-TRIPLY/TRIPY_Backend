package com.kmp.Triply.domain.course.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import com.kmp.Triply.domain.tourism.repository.TourismSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseSeedServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseSpotRepository courseSpotRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private TourismSpotRepository tourismSpotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 백제_스토리와_당일치기_실내_코스를_모두_등록한다() throws Exception {
        when(courseRepository.existsByTitle(anyString())).thenReturn(false);
        when(tourismSpotRepository.findByOpenApiContentId(anyString())).thenReturn(Optional.empty());
        when(tourismSpotRepository.save(any(TourismSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseSpotRepository.save(any(CourseSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseSeedService service = new CourseSeedService(
                courseRepository, courseSpotRepository, missionRepository, tourismSpotRepository, objectMapper);

        service.seedIfNeeded();

        // 여러 코스가 공유하는 관광지는 한 번만 생성된다(공산성·마곡사·독립기념관·에코리움 등).
        verify(tourismSpotRepository, times(27)).save(any(TourismSpot.class));
        // 백제 스토리 A/B/C + 지역별 당일치기 7개 + 실내 3개 = 코스 13개.
        verify(courseRepository, times(13)).save(any(Course.class));
        // 백제 스토리 4x3 + 당일치기(4+3+3+3+3+3+2) + 실내(3+2+2) = 40개.
        verify(courseSpotRepository, times(40)).save(any(CourseSpot.class));

        ArgumentCaptor<Mission> missionCaptor = ArgumentCaptor.forClass(Mission.class);
        verify(missionRepository, times(112)).save(missionCaptor.capture());

        Mission firstChoiceQuiz = missionCaptor.getAllValues().stream()
                .filter(mission -> mission.getMissionType() == MissionType.QUIZ_CHOICE)
                .filter(mission -> mission.getQuestion().contains("웅진으로 천도한 이유"))
                .findFirst()
                .orElseThrow();

        JsonNode choices = objectMapper.readTree(firstChoiceQuiz.getChoices());
        assertThat(choices).hasSize(4);
        long correctCount = 0;
        for (JsonNode choice : choices) {
            assertThat(choice.has("label")).isTrue();
            assertThat(choice.has("value")).isTrue();
            assertThat(choice.has("is_correct")).isTrue();
            if (choice.get("is_correct").asBoolean()) {
                correctCount++;
                assertThat(choice.get("value").asText()).isEqualTo("고구려의 침입");
            }
        }
        assertThat(correctCount).isEqualTo(1);
    }

    @Test
    void 아직_없는_코스만_추가로_등록한다() {
        when(courseRepository.existsByTitle(anyString()))
                .thenAnswer(invocation -> !"하루 만에 서해 낙조를 훔쳐라".equals(invocation.getArgument(0)));
        when(tourismSpotRepository.findByOpenApiContentId(anyString())).thenReturn(Optional.empty());
        when(tourismSpotRepository.save(any(TourismSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseSpotRepository.save(any(CourseSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseSeedService service = new CourseSeedService(
                courseRepository, courseSpotRepository, missionRepository, tourismSpotRepository, objectMapper);

        service.seedIfNeeded();

        // 당일치기 코스 1개(스팟 4개)만 등록되고, 그 코스가 쓰는 관광지 4곳만 생성된다.
        verify(courseRepository, times(1)).save(any(Course.class));
        verify(courseSpotRepository, times(4)).save(any(CourseSpot.class));
        verify(tourismSpotRepository, times(4)).save(any(TourismSpot.class));
    }

    @Test
    void 이미_등록되어_있으면_다시_등록하지_않는다() {
        when(courseRepository.existsByTitle(anyString())).thenReturn(true);

        CourseSeedService service = new CourseSeedService(
                courseRepository, courseSpotRepository, missionRepository, tourismSpotRepository, objectMapper);

        service.seedIfNeeded();

        verify(tourismSpotRepository, never()).save(any());
        verify(courseSpotRepository, never()).save(any());
        verify(missionRepository, never()).save(any());
    }
}
