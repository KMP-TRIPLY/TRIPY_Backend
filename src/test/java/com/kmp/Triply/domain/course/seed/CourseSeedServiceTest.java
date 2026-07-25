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
    void 스토리_ABC를_모두_등록한다() throws Exception {
        when(courseRepository.existsByTitle(anyString())).thenReturn(false);
        when(tourismSpotRepository.findByOpenApiContentId(anyString())).thenReturn(Optional.empty());
        when(tourismSpotRepository.save(any(TourismSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseSpotRepository.save(any(CourseSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseSeedService service = new CourseSeedService(
                courseRepository, courseSpotRepository, missionRepository, tourismSpotRepository, objectMapper);

        service.seedIfNeeded();

        // 관광지(공산성/마곡사/정림사지/궁남지)는 3개 스토리가 공유하므로 4번만 생성된다.
        verify(tourismSpotRepository, times(4)).save(any(TourismSpot.class));
        // 스토리 A/B/C = 코스 3개.
        verify(courseRepository, times(3)).save(any(Course.class));
        // 스토리당 4개 스팟 x 3 스토리 = 12개.
        verify(courseSpotRepository, times(12)).save(any(CourseSpot.class));

        ArgumentCaptor<Mission> missionCaptor = ArgumentCaptor.forClass(Mission.class);
        verify(missionRepository, times(28)).save(missionCaptor.capture());

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
