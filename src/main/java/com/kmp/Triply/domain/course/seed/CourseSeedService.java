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
        Map<String, TourismSpot> spotsByKey = ensureTourismSpots();
        List<StoryDef> missingStories = stories.stream()
                .filter(story -> !courseRepository.existsByTitle(story.title()))
                .toList();

        missingStories.forEach(story -> seedStory(story, spotsByKey));
        log.info("백제 역사 코스 시드 데이터 {}건을 등록했습니다.", missingStories.size());
    }

    private Map<String, TourismSpot> ensureTourismSpots() {
        Map<String, TourismSpot> result = new LinkedHashMap<>();
        result.put("GONGSANSEONG", ensureTourismSpot(
                "MANUAL-GONGSANSEONG", "공산성", "충청남도 공주시 금성동 65-3",
                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229)));
        result.put("SANSEONG_MARKET", ensureTourismSpot(
                "MANUAL-SANSEONG-MARKET", "공주 산성시장", "충청남도 공주시 산성시장5길 일원",
                BigDecimal.valueOf(36.4569), BigDecimal.valueOf(127.1258)));
        result.put("MURYEONG_ROYAL_TOMB", ensureTourismSpot(
                "MANUAL-MURYEONG-ROYAL-TOMB", "무령왕릉과 왕릉원", "충청남도 공주시 왕릉로 37",
                BigDecimal.valueOf(36.4621), BigDecimal.valueOf(127.1138)));
        result.put("JEMINCHEON", ensureTourismSpot(
                "MANUAL-JEMINCHEON", "제민천", "충청남도 공주시 제민천 일원",
                BigDecimal.valueOf(36.4545), BigDecimal.valueOf(127.1218)));
        result.put("GEUMGANG_BRIDGE", ensureTourismSpot(
                "MANUAL-GEUMGANG-BRIDGE", "금강교", "충청남도 공주시 금강교 일원",
                BigDecimal.valueOf(36.4660), BigDecimal.valueOf(127.1247)));
        result.put("MAGOKSA", ensureTourismSpot(
                "MANUAL-MAGOKSA", "마곡사", "충청남도 공주시 사곡면 마곡사로 966",
                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132)));
        result.put("JEONGNIMSAJI", ensureTourismSpot(
                "MANUAL-JEONGNIMSAJI", "정림사지", "충청남도 부여군 부여읍 정림로 83",
                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098)));
        result.put("GUNGNAMJI", ensureTourismSpot(
                "MANUAL-GUNGNAMJI", "궁남지", "충청남도 부여군 부여읍 궁남로 52",
                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209)));
        result.put("BUSOSANSEONG", ensureTourismSpot(
                "MANUAL-BUSOSANSEONG", "부소산성", "충청남도 부여군 부여읍 부소로 31",
                BigDecimal.valueOf(36.2817), BigDecimal.valueOf(126.9142)));
        result.put("BAEKJE_CULTURAL_LAND", ensureTourismSpot(
                "MANUAL-BAEKJE-CULTURAL-LAND", "백제문화단지", "충청남도 부여군 규암면 백제문로 455",
                BigDecimal.valueOf(36.3062), BigDecimal.valueOf(126.9027)));
        result.put("INDEPENDENCE_HALL", ensureTourismSpot(
                "MANUAL-INDEPENDENCE-HALL", "독립기념관", "충청남도 천안시 동남구 목천읍 독립기념관로 1",
                BigDecimal.valueOf(36.7819), BigDecimal.valueOf(127.2237)));
        result.put("YU_GWANSUN_HISTORIC_SITE", ensureTourismSpot(
                "MANUAL-YU-GWANSUN-HISTORIC-SITE", "유관순열사 사적지", "충청남도 천안시 동남구 병천면 유관순길 38",
                BigDecimal.valueOf(36.7619), BigDecimal.valueOf(127.3067)));
        result.put("HYEONCHUNGSA", ensureTourismSpot(
                "MANUAL-HYEONCHUNGSA", "현충사", "충청남도 아산시 염치읍 현충사길 126",
                BigDecimal.valueOf(36.8070), BigDecimal.valueOf(127.0140)));
        result.put("ONYANG_MARKET", ensureTourismSpot(
                "MANUAL-ONYANG-MARKET", "온양온천전통시장", "충청남도 아산시 시장길 일원",
                BigDecimal.valueOf(36.7835), BigDecimal.valueOf(127.0037)));
        result.put("KKOTJI_BEACH", ensureTourismSpot(
                "MANUAL-KKOTJI-BEACH", "꽃지해수욕장", "충청남도 태안군 안면읍 승언리",
                BigDecimal.valueOf(36.5018), BigDecimal.valueOf(126.3377)));
        result.put("ANMYONDO_RECREATION_FOREST", ensureTourismSpot(
                "MANUAL-ANMYONDO-RECREATION-FOREST", "안면도자연휴양림", "충청남도 태안군 안면읍 안면대로 3195-6",
                BigDecimal.valueOf(36.5262), BigDecimal.valueOf(126.3469)));
        result.put("SEOSAN_MAAE_TRIAD_BUDDHA", ensureTourismSpot(
                "MANUAL-SEOSAN-MAAE-TRIAD-BUDDHA", "서산 용현리 마애여래삼존상", "충청남도 서산시 운산면 마애삼존불길 65-13",
                BigDecimal.valueOf(36.7733), BigDecimal.valueOf(126.6067)));
        result.put("HAEMI_EUPSEONG", ensureTourismSpot(
                "MANUAL-HAEMI-EUPSEONG", "해미읍성", "충청남도 서산시 해미면 남문2로 143",
                BigDecimal.valueOf(36.7136), BigDecimal.valueOf(126.5458)));
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

    // ===== 스토리 원본 콘텐츠 =====

    private List<StoryDef> stories() {
        return List.of(
                storyGoldCrown(),
                storySabiSeal(),
                storyIndependenceTrail(),
                storyWestSeaChronicle(),
                storyA(),
                storyB(),
                storyC());
    }

    private StoryDef storyGoldCrown() {
        return new StoryDef(
                "사라진 백제 왕의 금관",
                "백제 왕실의 금관이 사라졌다. 참가자는 공주 곳곳에 남겨진 단서를 모아 금관을 훔친 범인과 보관 장소를 찾아야 한다.",
                "공주",
                List.of(
                        new SpotDef("GONGSANSEONG", (short) 1,
                                "서기 523년, 왕실에서 보관하던 금관이 사라졌다. 범인은 공주 곳곳에 네 개의 암호를 남겼다. "
                                        + "해가 지기 전 암호를 모아 금관을 되찾아라. 첫 번째 단서는 웅진백제의 왕성 공산성에 있다.",
                                BigDecimal.valueOf(36.4623), BigDecimal.valueOf(127.1277), 50,
                                List.of(
                                        photoScore("공산성 성문을 실제 장소에서 찾아 동일한 구도로 촬영하라.", 100, 20),
                                        choiceQuizScore("공산성이 백제의 왕성이었던 시기의 수도 이름은?", null, 100, 20,
                                                choice("①", "한성", false),
                                                choice("②", "웅진", true),
                                                choice("③", "사비", false),
                                                choice("④", "익산", false)),
                                        textQuizScore("첫 번째 암호를 입력하라.", "웅", 50, 0))),
                        new SpotDef("SANSEONG_MARKET", (short) 2,
                                "범인은 시장 상인에게 두 번째 암호를 맡겼다. 공주의 대표 먹거리를 찾아 암호를 얻어라.",
                                BigDecimal.valueOf(36.4569), BigDecimal.valueOf(127.1258), 80,
                                List.of(
                                        photoScore("시장 안에서 알밤 또는 밤으로 만든 상품을 찾아 촬영하라.", 100, 20),
                                        photoScore("팀원들이 밤 모양 손동작을 만들고 협동 사진을 촬영하라.", 50, 0),
                                        textQuizScore("두 번째 암호를 입력하라.", "진", 50, 0))),
                        new SpotDef("MURYEONG_ROYAL_TOMB", (short) 3,
                                "왕의 무덤에서 온 편지가 도착했다. 무령왕릉과 왕릉원에서 유물과 암호를 확인하라.",
                                BigDecimal.valueOf(36.4621), BigDecimal.valueOf(127.1138), 120,
                                List.of(
                                        choiceQuizScore("다음 중 무령왕릉에서 발견된 대표 유물은?", "무덤을 지키는 상징을 떠올려 보라.", 150, 20,
                                                choice("①", "무덤을 지키는 진묘수", true),
                                                choice("②", "백제금동대향로", false),
                                                choice("③", "익산 미륵사지 석탑 사리장엄구", false),
                                                choice("④", "첨성대", false)),
                                        textQuizScore("왕의 이름은 무령, 도읍은 웅진. 세 번째 암호는 왕이 잠든 곳의 첫 글자다.", "왕", 100, 20),
                                        photoScore("왕릉원 안 지정 포인트를 방문하고 진묘수 배지 인증 사진을 촬영하라.", 100, 0))),
                        new SpotDef("JEMINCHEON", (short) 4,
                                "범인은 물길을 따라 이동했다. 제민천을 따라 열린 글자를 조합해 마지막 장소를 찾아라.",
                                BigDecimal.valueOf(36.4545), BigDecimal.valueOf(127.1218), 100,
                                List.of(
                                        textQuizScore("제민천 포인트에서 열린 세 글자 '금/관/교'를 조합해 마지막 장소를 입력하라.", "금관교", 100, 0),
                                        choiceQuizScore("지금까지 모은 단서 웅/진/왕/금관교를 바탕으로 금관을 숨긴 인물을 고르라.", null, 100, 30,
                                                choice("①", "왕실 경비병", false),
                                                choice("②", "시장 상인", true),
                                                choice("③", "백제 왕을 돕는 시간 여행자", false),
                                                choice("④", "금관을 훔쳐 달아난 도굴꾼", false)))),
                        new SpotDef("GEUMGANG_BRIDGE", (short) 5,
                                "금관은 도난당한 것이 아니었다. 전쟁의 위험을 피해 백제 백성이 함께 숨긴 것이었다. "
                                        + "왕국을 지킨 것은 왕이 아니라 백성이었다.",
                                BigDecimal.valueOf(36.4660), BigDecimal.valueOf(127.1247), 80,
                                List.of(
                                        photoScore("금강교 또는 공산성이 보이는 지정 지점에서 금관 회수 완료 인증 사진을 촬영하라.", 200, 0),
                                        textQuizScore("최종 칭호 조건: 총점 800점 이상은 백제 수호자, 950점 이상은 무엇인가?", "무령왕의 밀사", 50, 0)))
                ));
    }

    private StoryDef storySabiSeal() {
        return new StoryDef(
                "사비의 마지막 봉인을 풀어라",
                "사비 백제의 마지막 기록이 네 개의 봉인으로 나뉘어 사라졌다. 부여의 핵심 유적을 따라 이동하며 왕도 사비의 기억을 복원하라.",
                "부여",
                List.of(
                        new SpotDef("JEONGNIMSAJI", (short) 1,
                                "첫 번째 봉인은 정림사지 오층석탑 앞에 있다. 백제의 중심 사찰이 남긴 균형과 절제의 단서를 찾아라.",
                                BigDecimal.valueOf(36.2762), BigDecimal.valueOf(126.9098), 50,
                                List.of(
                                        choiceQuizScore("정림사지의 대표 유적으로 가장 알맞은 것은?", null, 100, 20,
                                                choice("①", "오층석탑", true),
                                                choice("②", "첨성대", false),
                                                choice("③", "동궁과 월지", false),
                                                choice("④", "석굴암", false)),
                                        photoScore("정림사지 오층석탑이 보이는 위치에서 팀 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("첫 번째 봉인의 글자를 입력하라.", "사", 50, 0))),
                        new SpotDef("BUSOSANSEONG", (short) 2,
                                "두 번째 봉인은 부소산성의 숲길에 숨겨져 있다. 왕성을 지키던 길을 따라 사비의 방어선을 확인하라.",
                                BigDecimal.valueOf(36.2817), BigDecimal.valueOf(126.9142), 120,
                                List.of(
                                        choiceQuizScore("부소산성은 백제 어느 수도 시기와 가장 관련이 깊은가?", null, 100, 20,
                                                choice("①", "한성", false),
                                                choice("②", "웅진", false),
                                                choice("③", "사비", true),
                                                choice("④", "고려", false)),
                                        photoScore("부소산성 산책로 또는 성곽 안내판을 배경으로 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("두 번째 봉인의 글자를 입력하라.", "비", 50, 0))),
                        new SpotDef("GUNGNAMJI", (short) 3,
                                "세 번째 봉인은 왕궁의 정원 연못에 잠겨 있다. 궁남지의 물길을 따라 왕실 정원의 단서를 모아라.",
                                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209), 150,
                                List.of(
                                        choiceQuizScore("궁남지와 가장 관련 깊은 설명은?", null, 100, 20,
                                                choice("①", "왕궁 정원 연못", true),
                                                choice("②", "산성의 군사 창고", false),
                                                choice("③", "조선 시대 서원", false),
                                                choice("④", "근대 철도역", false)),
                                        photoScore("궁남지 포룡정 또는 연못이 보이는 위치에서 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("세 번째 봉인의 글자를 입력하라.", "왕", 50, 0))),
                        new SpotDef("BAEKJE_CULTURAL_LAND", (short) 4,
                                "마지막 봉인은 복원된 백제 왕궁의 문양 속에 있다. 흩어진 글자를 조합해 사비의 마지막 기록을 완성하라.",
                                BigDecimal.valueOf(36.3062), BigDecimal.valueOf(126.9027), 200,
                                List.of(
                                        photoScore("백제문화단지의 궁궐 또는 백제 양식 건축물을 배경으로 완료 인증 사진을 촬영하라.", 150, 0),
                                        choiceQuizScore("지금까지 모은 글자 사/비/왕을 완성하는 마지막 단어로 가장 알맞은 것은?", null, 100, 30,
                                                choice("①", "도", true),
                                                choice("②", "산", false),
                                                choice("③", "해", false),
                                                choice("④", "문", false)),
                                        textQuizScore("최종 암호를 입력하라.", "사비왕도", 150, 0)))
                ));
    }

    private StoryDef storyIndependenceTrail() {
        return new StoryDef(
                "독립의 길을 따라",
                "충남에 남은 독립운동의 흔적을 따라가며 사라진 태극기 암호를 복원한다. 참가자는 기억, 용기, 헌신의 단서를 모아 최종 메시지를 완성해야 한다.",
                "천안·아산",
                List.of(
                        new SpotDef("INDEPENDENCE_HALL", (short) 1,
                                "첫 번째 단서는 독립기념관에 있다. 전시와 상징물을 관찰해 독립의 시작을 기억하라.",
                                BigDecimal.valueOf(36.7819), BigDecimal.valueOf(127.2237), 200,
                                List.of(
                                        choiceQuizScore("독립기념관의 주제와 가장 가까운 것은?", null, 100, 20,
                                                choice("①", "독립운동과 민족의 역사", true),
                                                choice("②", "해양 생태 체험", false),
                                                choice("③", "온천 휴양", false),
                                                choice("④", "백제 왕궁 복원", false)),
                                        photoScore("독립기념관 상징 조형물 또는 전시관 안내판을 배경으로 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("첫 번째 암호를 입력하라.", "기억", 50, 0))),
                        new SpotDef("YU_GWANSUN_HISTORIC_SITE", (short) 2,
                                "두 번째 단서는 아우내의 함성 속에 있다. 유관순 열사의 흔적을 따라 용기의 의미를 찾아라.",
                                BigDecimal.valueOf(36.7619), BigDecimal.valueOf(127.3067), 150,
                                List.of(
                                        choiceQuizScore("유관순 열사와 가장 관련 깊은 운동은?", null, 100, 20,
                                                choice("①", "3·1운동", true),
                                                choice("②", "갑신정변", false),
                                                choice("③", "동학농민운동", false),
                                                choice("④", "새마을운동", false)),
                                        photoScore("유관순열사 사적지 안내판 또는 기념 공간에서 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("두 번째 암호를 입력하라.", "용기", 50, 0))),
                        new SpotDef("HYEONCHUNGSA", (short) 3,
                                "세 번째 단서는 현충사에 있다. 나라를 지킨 헌신의 기록을 확인하고 다음 암호를 얻어라.",
                                BigDecimal.valueOf(36.8070), BigDecimal.valueOf(127.0140), 180,
                                List.of(
                                        choiceQuizScore("현충사는 누구를 기리는 사당인가?", null, 100, 20,
                                                choice("①", "이순신", true),
                                                choice("②", "김구", false),
                                                choice("③", "정약용", false),
                                                choice("④", "세종대왕", false)),
                                        photoScore("현충사 입구 또는 충무공 관련 안내판을 배경으로 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("세 번째 암호를 입력하라.", "헌신", 50, 0))),
                        new SpotDef("ONYANG_MARKET", (short) 4,
                                "마지막 단서는 오늘의 지역 일상 속에 있다. 시장에서 사람들의 삶을 확인하고 태극기 암호를 완성하라.",
                                BigDecimal.valueOf(36.7835), BigDecimal.valueOf(127.0037), 120,
                                List.of(
                                        photoScore("온양온천전통시장에서 지역 상점 또는 먹거리를 배경으로 팀 인증 사진을 촬영하라.", 100, 0),
                                        choiceQuizScore("기억/용기/헌신을 이어 최종 메시지로 가장 알맞은 것은?", null, 100, 30,
                                                choice("①", "대한독립", true),
                                                choice("②", "왕실금관", false),
                                                choice("③", "사비왕도", false),
                                                choice("④", "바다노을", false)),
                                        textQuizScore("최종 암호를 입력하라.", "대한독립", 200, 0)))
                ));
    }

    private StoryDef storyWestSeaChronicle() {
        return new StoryDef(
                "서해 시간 조각을 찾아라",
                "서해 바람에 흩어진 시간 조각이 태안과 서산 곳곳에 떨어졌다. 바다, 숲, 불상, 읍성을 지나며 충남 서해안의 기억을 완성하라.",
                "태안·서산",
                List.of(
                        new SpotDef("KKOTJI_BEACH", (short) 1,
                                "첫 번째 시간 조각은 바다와 노을 사이에 있다. 꽃지해수욕장에서 서해의 방향을 확인하라.",
                                BigDecimal.valueOf(36.5018), BigDecimal.valueOf(126.3377), 150,
                                List.of(
                                        choiceQuizScore("꽃지해수욕장과 가장 잘 어울리는 키워드는?", null, 100, 20,
                                                choice("①", "서해 노을", true),
                                                choice("②", "고산 설경", false),
                                                choice("③", "백제 왕릉", false),
                                                choice("④", "근대 역사관", false)),
                                        photoScore("꽃지해수욕장 바다 또는 해변 안내판을 배경으로 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("첫 번째 시간 조각을 입력하라.", "바다", 50, 0))),
                        new SpotDef("ANMYONDO_RECREATION_FOREST", (short) 2,
                                "두 번째 시간 조각은 소나무 숲길에 있다. 안면도 숲에서 바다와 이어지는 생태 단서를 찾아라.",
                                BigDecimal.valueOf(36.5262), BigDecimal.valueOf(126.3469), 150,
                                List.of(
                                        photoScore("안면도자연휴양림 숲길 또는 안내판을 배경으로 인증 사진을 촬영하라.", 100, 0),
                                        choiceQuizScore("안면도자연휴양림 미션의 핵심 장소로 가장 알맞은 것은?", null, 100, 20,
                                                choice("①", "숲길", true),
                                                choice("②", "성곽", false),
                                                choice("③", "시장", false),
                                                choice("④", "왕릉", false)),
                                        textQuizScore("두 번째 시간 조각을 입력하라.", "숲", 50, 0))),
                        new SpotDef("SEOSAN_MAAE_TRIAD_BUDDHA", (short) 3,
                                "세 번째 시간 조각은 바위에 새겨진 미소 속에 있다. 서산 마애여래삼존상에서 오래된 신앙의 흔적을 확인하라.",
                                BigDecimal.valueOf(36.7733), BigDecimal.valueOf(126.6067), 120,
                                List.of(
                                        choiceQuizScore("서산 용현리 마애여래삼존상은 어떤 형태의 문화유산인가?", null, 100, 20,
                                                choice("①", "바위에 새긴 불상", true),
                                                choice("②", "목조 궁궐", false),
                                                choice("③", "근대 철도역", false),
                                                choice("④", "해변 전망대", false)),
                                        photoScore("마애여래삼존상 안내판 또는 관람 동선에서 인증 사진을 촬영하라.", 100, 0),
                                        textQuizScore("세 번째 시간 조각을 입력하라.", "미소", 50, 0))),
                        new SpotDef("HAEMI_EUPSEONG", (short) 4,
                                "마지막 시간 조각은 읍성의 문 안에 있다. 해미읍성에서 방어와 생활의 흔적을 찾아 최종 문장을 완성하라.",
                                BigDecimal.valueOf(36.7136), BigDecimal.valueOf(126.5458), 150,
                                List.of(
                                        photoScore("해미읍성 성문 또는 성곽을 배경으로 완료 인증 사진을 촬영하라.", 100, 0),
                                        choiceQuizScore("바다/숲/미소를 이어 완성할 마지막 키워드로 가장 알맞은 것은?", null, 100, 30,
                                                choice("①", "읍성", true),
                                                choice("②", "온천", false),
                                                choice("③", "왕도", false),
                                                choice("④", "금관", false)),
                                        textQuizScore("최종 암호를 입력하라.", "서해의 시간", 200, 0)))
                ));
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

    private static MissionDef choiceQuizScore(String question, String hint, int baseScore, int hintPenalty, ChoiceDef... choices) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null, List.of(choices), hint, hintPenalty, baseScore);
    }

    private static MissionDef oxQuiz(String question, boolean answerIsO, String hint) {
        return new MissionDef(MissionType.QUIZ_CHOICE, question, null,
                List.of(choice("O", "O", answerIsO), choice("X", "X", !answerIsO)), hint, 100, 200);
    }

    private static MissionDef textQuiz(String question, String answer) {
        return new MissionDef(MissionType.QUIZ_TEXT, question, answer, null, null, 150, 300);
    }

    private static MissionDef textQuizScore(String question, String answer, int baseScore, int hintPenalty) {
        return new MissionDef(MissionType.QUIZ_TEXT, question, answer, null, null, hintPenalty, baseScore);
    }

    private static MissionDef photo(String question) {
        return new MissionDef(MissionType.PHOTO, question, null, null, null, 0, 100);
    }

    private static MissionDef photoScore(String question, int baseScore, int hintPenalty) {
        return new MissionDef(MissionType.PHOTO, question, null, null, null, hintPenalty, baseScore);
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
