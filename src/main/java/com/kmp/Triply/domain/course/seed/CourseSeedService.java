package com.kmp.Triply.domain.course.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.CourseType;
import com.kmp.Triply.domain.course.entity.Difficulty;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.course.entity.MissionType;
import com.kmp.Triply.domain.course.repository.CourseRepository;
import com.kmp.Triply.domain.course.repository.CourseSpotRepository;
import com.kmp.Triply.domain.course.repository.MissionRepository;
import com.kmp.Triply.domain.tourism.entity.SpotCategory;
import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import com.kmp.Triply.domain.tourism.repository.TourismSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 공주·부여 백제 역사 코스(스토리 A/B/C)의 스팟·미션(퀴즈) 데이터를 최초 1회 등록한다.
 * app.seed.enabled=true 일 때 {@link CourseSeedRunner}가 애플리케이션 기동 시 호출한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSeedService {

    // 국가행정표준코드 시도 코드(충청남도=44). 관광공사 API 연동부(TourismApiServiceImpl)와 동일 기준.
    private static final String REGION_CODE = "44";

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final MissionRepository missionRepository;
    private final TourismSpotRepository tourismSpotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void seedIfNeeded() {
        List<StoryDef> stories = stories();
        boolean alreadySeeded = stories.stream().anyMatch(story -> courseRepository.existsByTitle(story.title()));
        if (alreadySeeded) {
            log.info("백제 역사 코스 시드 데이터가 이미 존재합니다. 시딩을 건너뜁니다.");
            return;
        }

        Map<String, TourismSpot> spotsByKey = ensureTourismSpots();
        stories.forEach(story -> seedStory(story, spotsByKey));
        log.info("백제 역사 코스(스토리 A/B/C) 시드 데이터 {}건을 등록했습니다.", stories.size());
    }

    private Map<String, TourismSpot> ensureTourismSpots() {
        Map<String, TourismSpot> result = new LinkedHashMap<>();
        result.put("GONGSANSEONG", ensureTourismSpot(
                "MANUAL-GONGSANSEONG", "공산성", "충청남도 공주시 금성동 65-3",
                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229)));
        result.put("MAGOKSA", ensureTourismSpot(
                "MANUAL-MAGOKSA", "마곡사", "충청남도 공주시 사곡면 마곡사로 966",
                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132)));
        result.put("JEONGNIMSAJI", ensureTourismSpot(
                "MANUAL-JEONGNIMSAJI", "정림사지", "충청남도 부여군 부여읍 정림로 83",
                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098)));
        result.put("GUNGNAMJI", ensureTourismSpot(
                "MANUAL-GUNGNAMJI", "궁남지", "충청남도 부여군 부여읍 궁남로 52",
                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209)));
        return result;
    }

    private TourismSpot ensureTourismSpot(String contentId, String name, String address, BigDecimal lat, BigDecimal lng) {
        return tourismSpotRepository.findByOpenApiContentId(contentId)
                .orElseGet(() -> tourismSpotRepository.save(TourismSpot.builder()

                        .openApiContentId(contentId)
                        .name(name)
                        .category(SpotCategory.HERITAGE)
                        .address(address)
                        .lat(lat)
                        .lng(lng)
                        .areaCode(REGION_CODE)
                        .rank(null)
                        .build()));
    }

    private void seedStory(StoryDef story, Map<String, TourismSpot> spotsByKey) {
        Course course = courseRepository.save(Course.builder()
                .title(story.title())
                .description(story.description())
                .regionCode(REGION_CODE)
                .city(story.city())
                .difficulty(Difficulty.NORMAL)
                .estimatedMinutes(180)
                .courseType(CourseType.GENERAL)
                .isAiGenerated(false)
                .createdBy(null)
                .build());

        story.spots().forEach(spotDef -> {
            CourseSpot courseSpot = courseSpotRepository.save(CourseSpot.builder()
                    .course(course)
                    .tourismSpot(spotsByKey.get(spotDef.siteKey()))
                    .sequenceOrder(spotDef.sequenceOrder())
                    .storyText(spotDef.storyText())
                    .lat(spotDef.lat())
                    .lng(spotDef.lng())
                    .radiusMeters(spotDef.radiusMeters())
                    .build());

            spotDef.missions().forEach(missionDef -> missionRepository.save(Mission.builder()
                    .courseSpot(courseSpot)
                    .missionType(missionDef.type())
                    .question(missionDef.question())
                    .answer(missionDef.answer())
                    .choices(toJson(missionDef.choices()))
                    .hint(missionDef.hint())
                    .hintPenalty(missionDef.hintPenalty())
                    .baseScore(missionDef.baseScore())
                    .build()));
        });
    }

    private String toJson(List<ChoiceDef> choices) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> payload = new ArrayList<>();
        for (ChoiceDef choice : choices) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("label", choice.label());
            entry.put("value", choice.value());
            entry.put("is_correct", choice.correct());
            payload.add(entry);
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("코스 시드 데이터 직렬화에 실패했습니다.", e);
        }
    }

    // ===== 스토리 A/B/C 원본 콘텐츠 =====

    private List<StoryDef> stories() {
        return List.of(storyA(), storyB(), storyC());
    }

    private StoryDef storyA() {
        return new StoryDef(
                "백제 무령왕의 왕실 인장을 찾아라",
                "백제 무령왕의 왕실 인장이 사라졌다. 역사 탐정인 당신은 공주와 부여에 흩어진 단서를 모아 인장의 행방을 밝혀야 한다.",
                "공주·부여",
                List.of(
                        new SpotDef("GONGSANSEONG", (short) 1,
                                "첫 번째 단서는 백제가 웅진으로 천도한 이유 속에 숨어있다. 성벽 위에서 금강을 바라보며 왕이 이 땅을 선택한 이유를 찾아라. "
                                        + "(공산성 쌍수정 반경 30m 이내 진입)",
                                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229), 30,
                                List.of(
                                        choiceQuiz("백제가 웅진으로 천도한 이유와 가장 관련 깊은 것은?", null,
                                                choice("①", "한강 유역 홍수", false),
                                                choice("②", "고구려의 침입", true),
                                                choice("③", "신라와의 동맹", false),
                                                choice("④", "당나라 요청", false)),
                                        photo("성벽 위에서 금강이 보이는 방향으로 팀 전원 인증샷"))),
                        new SpotDef("MAGOKSA", (short) 2,
                                "다음 단서: 강을 따라 내려가면 왕의 흔적이 있다. 인장을 숨긴 자는 속세를 떠나 이 사찰로 향했다. "
                                        + "대웅보전의 비밀 속에 두 번째 단서가 있다.",
                                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132), 200,
                                List.of(
                                        textQuiz("마곡사에서 김구 선생이 은거할 당시 사용한 법명은?", "원종"),
                                        oxQuiz("마곡사는 유네스코 세계문화유산에 등재되어 있다", true, null),
                                        photo("대웅보전 현판 앞에서 팀 전원 인증샷"))),
                        new SpotDef("JEONGNIMSAJI", (short) 3,
                                "다음 단서: 왕의 무덤으로 향하라. 인장은 백제 멸망의 현장 근처에 숨겨졌다. "
                                        + "당나라 장수가 승전을 새긴 석탑에서 세 번째 단서를 찾아라. (정림사지 5층석탑 반경 20m 이내 진입)",
                                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098), 20,
                                List.of(
                                        choiceQuiz("정림사지 석탑에 승전 기록을 새긴 당나라 장수는?", null,
                                                choice("①", "이세민", false),
                                                choice("②", "소정방", true),
                                                choice("③", "설인귀", false),
                                                choice("④", "이적", false)),
                                        textQuiz("백제 멸망 연도는?", "660년"))),
                        new SpotDef("GUNGNAMJI", (short) 4,
                                "마지막 단서는 백제 왕궁의 정원 연못 속에 있다. 연꽃이 만발한 이곳에서 인장의 최종 위치를 밝혀라.",
                                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209), 200,
                                List.of(
                                        choiceQuiz("궁남지는 우리나라 최초의 무엇인가?", null,
                                                choice("①", "저수지", false),
                                                choice("②", "인공연못", true),
                                                choice("③", "왕궁정원", false),
                                                choice("④", "인공섬", false)),
                                        photo("포룡정 다리 위에서 연꽃 배경으로 팀 전원 인증샷 — 인장을 찾았다!")))
                ));
    }

    private StoryDef storyB() {
        return new StoryDef(
                "백제 마지막 왕의 원혼이 남긴 수수께끼",
                "백제 의자왕의 원혼이 천 년 넘게 공주와 부여를 떠돌고 있다. 원혼을 달래기 위해 그가 남긴 수수께끼를 풀어라.",
                "공주·부여",
                List.of(
                        new SpotDef("GONGSANSEONG", (short) 1,
                                "의자왕의 원혼이 속삭인다. '나의 선조가 이 성을 쌓은 이유를 아는 자만이 첫 번째 봉인을 풀 수 있다.' "
                                        + "(공산성 금서루 반경 30m 이내 진입)",
                                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229), 30,
                                List.of(
                                        oxQuiz("공산성은 백제 시대 흙으로만 쌓은 토성이었다", true,
                                                "조선시대에 석성으로 개축되었습니다."),
                                        photo("공산성 성문 앞에서 원혼을 달래는 듯한 포즈로 팀 인증샷"))),
                        new SpotDef("MAGOKSA", (short) 2,
                                "다음 단서: 원혼은 승려가 머물던 곳으로 향한다. 원혼이 이끄는 곳은 천년 고찰. "
                                        + "'내 왕국이 무너질 때 이곳의 승려들은 무엇을 했는가?'",
                                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132), 200,
                                List.of(
                                        choiceQuiz("마곡사가 창건된 시기는?", null,
                                                choice("①", "삼국시대", true),
                                                choice("②", "통일신라", false),
                                                choice("③", "고려", false),
                                                choice("④", "조선", false)),
                                        textQuiz("마곡사 경내를 흐르는 하천 이름은?", "마곡천"),
                                        photo("마곡사 연못 앞에서 원혼에게 묵념하는 포즈로 팀 인증샷"))),
                        new SpotDef("JEONGNIMSAJI", (short) 3,
                                "다음 단서: 멸망의 현장으로 가라. 원혼이 가장 슬퍼하는 곳. "
                                        + "'당나라 장수가 내 나라 멸망을 이 탑에 새겼다. 그 치욕의 기록을 찾아라.' "
                                        + "(정림사지 박물관 입구 반경 20m 이내 진입)",
                                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098), 20,
                                List.of(
                                        choiceQuiz("의자왕이 당나라로 끌려간 해는?", null,
                                                choice("①", "658년", false),
                                                choice("②", "660년", true),
                                                choice("③", "663년", false),
                                                choice("④", "668년", false)),
                                        oxQuiz("정림사지 5층석탑은 목탑 양식을 본뜬 석탑이다", true, null))),
                        new SpotDef("GUNGNAMJI", (short) 4,
                                "원혼의 마지막 말. '내가 연회를 즐기던 이 연못에서 봉인을 풀어다오. 그래야 나도 편히 쉴 수 있다.'",
                                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209), 200,
                                List.of(
                                        textQuiz("삼국유사에서 궁남지와 관련된 왕은 누구인가?", "무왕"),
                                        photo("궁남지 포룡정에서 원혼을 해방시키는 듯한 포즈로 팀 인증샷")))
                ));
    }

    private StoryDef storyC() {
        return new StoryDef(
                "백제 부흥군의 비밀 작전을 완수하라",
                "백제 멸망 후 부흥군이 숨겨둔 비밀 무기고의 위치가 담긴 지도가 발견됐다. 당신은 부흥군의 후예로서 작전을 완수해야 한다.",
                "공주·부여",
                List.of(
                        new SpotDef("GONGSANSEONG", (short) 1,
                                "작전 브리핑: 백제 부흥군이 최후 항전을 준비했던 요새. 성벽을 순찰하며 적군(나당연합군)의 동태를 파악하라. "
                                        + "(공산성 동문 반경 30m 이내 진입)",
                                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229), 30,
                                List.of(
                                        choiceQuiz("백제 부흥운동을 이끈 대표적인 인물은?", null,
                                                choice("①", "흑치상지", true),
                                                choice("②", "계백", false),
                                                choice("③", "성충", false),
                                                choice("④", "흥수", false)),
                                        photo("성벽 위에서 망을 보는 척 팀 전원 작전 포즈 인증샷"))),
                        new SpotDef("MAGOKSA", (short) 2,
                                "다음 단서: 아군이 은거한 사찰로 이동하라. 작전 2구역: 부흥군이 군사를 숨기고 보급품을 저장했던 사찰. "
                                        + "은닉된 보급품의 수량 암호를 해독하라.",
                                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132), 200,
                                List.of(
                                        oxQuiz("마곡사는 6.25 전쟁 당시 피해를 입지 않은 사찰이다", true, null),
                                        textQuiz("마곡사 대광보전 앞 석등은 몇 층 구조인가?", "4층"),
                                        photo("마곡사 일주문 앞에서 부흥군 입성 포즈로 팀 인증샷"))),
                        new SpotDef("JEONGNIMSAJI", (short) 3,
                                "다음 단서: 멸망의 현장에서 결의를 다져라. 작전 3구역: 나당연합군이 승전을 새긴 치욕의 탑 앞에서 "
                                        + "부흥의 결의를 다져라. 적의 전략을 역으로 이용하라. (정림사지 5층석탑 반경 20m 이내 진입)",
                                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098), 20,
                                List.of(
                                        choiceQuiz("백제 부흥운동이 최종 실패한 전투는?", null,
                                                choice("①", "황산벌 전투", false),
                                                choice("②", "백강 전투", true),
                                                choice("③", "매소성 전투", false),
                                                choice("④", "기벌포 전투", false)),
                                        photo("석탑 앞에서 부흥군의 결의를 다지는 포즈로 팀 인증샷"))),
                        new SpotDef("GUNGNAMJI", (short) 4,
                                "다음 단서: 최후의 비밀 무기고는 왕궁 연못 근처에 있다. 최종 작전: 비밀 무기고의 마지막 암호는 "
                                        + "이 연못의 역사 속에 있다. 암호를 해독하고 작전을 완수하라!",
                                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209), 200,
                                List.of(
                                        choiceQuiz("궁남지가 축조된 것으로 기록된 삼국사기 연도는?", null,
                                                choice("①", "600년", false),
                                                choice("②", "634년", true),
                                                choice("③", "660년", false),
                                                choice("④", "678년", false)),
                                        textQuiz("궁남지 인공섬 위에 세워진 정자 이름은?", "포룡정"),
                                        photo("포룡정 앞에서 작전 완수 승리 포즈로 팀 전원 인증샷")))
                ));
    }

    // ===== 미션 정의 헬퍼 =====

    private static MissionDef choiceQuiz(String question, String hint, ChoiceDef... choices) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null, List.of(choices), hint, 150, 300);
    }

    private static MissionDef oxQuiz(String question, boolean answerIsO, String hint) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null,
                List.of(choice("O", "O", answerIsO), choice("X", "X", !answerIsO)), hint, 100, 200);
    }

    private static MissionDef textQuiz(String question, String answer) {
        return new MissionDef(MissionType.QUIZ_TEXT, question, answer, null, null, 150, 300);
    }

    private static MissionDef photo(String question) {
        return new MissionDef(MissionType.PHOTO, question, null, null, null, 0, 100);
    }

    private static ChoiceDef choice(String label, String value, boolean correct) {
        return new ChoiceDef(label, value, correct);
    }

    // ===== 시드 전용 내부 모델 =====

    private record ChoiceDef(String label, String value, boolean correct) {
    }

    private record MissionDef(
            MissionType type, String question, String answer,
            List<ChoiceDef> choices, String hint, int hintPenalty, int baseScore) {
    }

    private record SpotDef(
            String siteKey, short sequenceOrder, String storyText,
            BigDecimal lat, BigDecimal lng, int radiusMeters, List<MissionDef> missions) {
    }

    private record StoryDef(String title, String description, String city, List<SpotDef> spots) {
    }
}
