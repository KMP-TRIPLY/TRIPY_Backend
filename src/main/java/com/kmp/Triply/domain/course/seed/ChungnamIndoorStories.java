package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.course.entity.CourseTag;
import com.kmp.Triply.domain.course.entity.Difficulty;
import com.kmp.Triply.domain.course.entity.IndoorType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.kmp.Triply.domain.course.seed.SeedMissions.choice;
import static com.kmp.Triply.domain.course.seed.SeedMissions.choiceQuiz;
import static com.kmp.Triply.domain.course.seed.SeedMissions.oxQuiz;
import static com.kmp.Triply.domain.course.seed.SeedMissions.photo;
import static com.kmp.Triply.domain.course.seed.SeedMissions.textQuiz;

/**
 * 전부 실내에서 도는 충남 코스. 비·눈이 오거나 한여름·한겨울에 추천된다.
 * 모든 스팟이 실내라 날씨로 일정이 무너지지 않고, 이동 구간도 짧게 잡았다.
 */
final class ChungnamIndoorStories {

    /** 궂은 날씨에 공통으로 붙는 태그. 비·폭염·한파를 모두 피할 수 있다. */
    private static final Set<CourseTag> ALL_WEATHER = Set.of(
            CourseTag.RAINY_DAY, CourseTag.HOT_DAY, CourseTag.COLD_DAY,
            CourseTag.MUSEUM, CourseTag.KID_FRIENDLY, CourseTag.LIGHT_WALK);

    private ChungnamIndoorStories() {
    }

    static List<StoryDef> stories() {
        return List.of(buyeoIndoor(), seocheonIndoor(), cheonanAsanIndoor());
    }

    /** 부여 실내 — 박물관 세 곳으로 사비를 훑는다. */
    private static StoryDef buyeoIndoor() {
        return new StoryDef(
                "비 오는 날의 사비",
                "사비의 하루가 비에 잠겼다. 부소산에 오르지 못하는 날, 백제의 조각은 모두 지붕 아래로 물러나 있다. "
                        + "박물관 세 곳에 나뉘어 놓인 조각을 이어 붙여 사라진 하루를 복원하라. "
                        + "우산은 옮겨 갈 때만 펴면 된다.",
                "부여",
                Difficulty.EASY,
                360,
                IndoorType.INDOOR,
                union(ALL_WEATHER, CourseTag.HISTORY),
                List.of(
                        new SpotDef("BUYEO_MUSEUM", (short) 1,
                                "첫 조각은 백제 공예의 정점에 있다. 뚜껑 위의 봉황부터 받침의 용까지, "
                                        + "한 덩어리로 주조된 그 향로 앞에서 시작한다.",
                                BigDecimal.valueOf(36.2758), BigDecimal.valueOf(126.9147), 150, true,
                                List.of(
                                        choiceQuiz("국립부여박물관을 대표하는 백제 유물은?", "국보로 지정된 향로입니다.",
                                                choice("①", "백제금동대향로", true),
                                                choice("②", "금관총 금관", false),
                                                choice("③", "성덕대왕신종", false),
                                                choice("④", "천마도", false)),
                                        oxQuiz("백제금동대향로는 국보로 지정되어 있다", true, null),
                                        photo("향로 전시실 앞에서 팀 전원 인증샷"))),
                        new SpotDef("JEONGNIMSAJI_MUSEUM", (short) 2,
                                "[이동] 두 번째 조각. 밖에 선 석탑은 비를 맞고 있지만, 그 탑의 이야기는 옆 건물 안에 다 있다.",
                                BigDecimal.valueOf(36.2768), BigDecimal.valueOf(126.9089), 100, true,
                                List.of(
                                        textQuiz("정림사지에 남아 있는 석탑은 몇 층 석탑인가? (예: 5층)", "5층"),
                                        choiceQuiz("정림사지 석탑에 승전 기록을 새긴 당나라 장수는?", null,
                                                choice("①", "이세민", false),
                                                choice("②", "소정방", true),
                                                choice("③", "설인귀", false),
                                                choice("④", "이적", false)),
                                        photo("전시관 안 정림사 복원 모형 앞에서 팀 전원 인증샷"))),
                        new SpotDef("BAEKJE_HISTORY_MUSEUM", (short) 3,
                                "[최종] 마지막 조각. 백제 700년이 한 층에 정리되어 있다. 도읍이 옮겨 간 순서를 맞히면 하루가 끝난다.",
                                BigDecimal.valueOf(36.3138), BigDecimal.valueOf(126.8898), 150, true,
                                List.of(
                                        choiceQuiz("백제의 도읍이 옮겨 간 순서로 맞는 것은?", null,
                                                choice("①", "한성 → 웅진 → 사비", true),
                                                choice("②", "웅진 → 한성 → 사비", false),
                                                choice("③", "사비 → 웅진 → 한성", false),
                                                choice("④", "한성 → 사비 → 웅진", false)),
                                        textQuiz("백제의 마지막 도읍 이름은?", "사비"),
                                        photo("전시관 안에서 팀 전원 인증샷 — 비 오는 날의 사비 완주!")))
                ));
    }

    /** 서천 실내 — 기후대와 바다를 건물 안에서 건넌다. */
    private static StoryDef seocheonIndoor() {
        return new StoryDef(
                "빗속의 금강 하구 표본실",
                "밖은 비, 안은 열대다. 건물 두 채 안에 지구의 기후대와 서해 바닷속이 통째로 들어와 있다. "
                        + "비 한 방울 맞지 않고 두 세계를 건너 표본을 모아라.",
                "서천",
                Difficulty.EASY,
                300,
                IndoorType.INDOOR,
                union(ALL_WEATHER, CourseTag.NATURE),
                List.of(
                        new SpotDef("ECOREUM", (short) 1,
                                "탐사 1구역. 문 하나를 지날 때마다 기후가 바뀐다. 몇 개의 세계를 지나왔는지 세어 두어라.",
                                BigDecimal.valueOf(36.0243), BigDecimal.valueOf(126.7205), 250, true,
                                List.of(
                                        choiceQuiz("국립생태원 에코리움이 재현한 기후대는 모두 몇 개인가?",
                                                "열대·사막·지중해·온대·극지",
                                                choice("①", "3개", false),
                                                choice("②", "4개", false),
                                                choice("③", "5개", true),
                                                choice("④", "7개", false)),
                                        textQuiz("국립생태원이 있는 충남의 시·군은?", "서천군"),
                                        photo("열대관 안에서 팀 전원 인증샷"))),
                        new SpotDef("MARINE_BIO_RESOURCES", (short) 2,
                                "[최종] 탐사 2구역. 이번엔 바닷속이다. 비를 한 방울도 맞지 않고 서해 생물을 전부 만난다.",
                                BigDecimal.valueOf(36.0056), BigDecimal.valueOf(126.6875), 150, true,
                                List.of(
                                        choiceQuiz("국립해양생물자원관이 수집·연구하는 대상은?", null,
                                                choice("①", "해양생물", true),
                                                choice("②", "광물", false),
                                                choice("③", "곤충", false),
                                                choice("④", "고문서", false)),
                                        oxQuiz("'씨큐리움'은 국립해양생물자원관의 전시관 이름이다", true, null),
                                        photo("전시관 안 표본 앞에서 팀 전원 인증샷 — 표본실 탐사 완료!")))
                ));
    }

    /** 천안·아산 실내 — 전시관 두 곳. */
    private static StoryDef cheonanAsanIndoor() {
        return new StoryDef(
                "우산 쓰고 가는 겨레의 기억",
                "비가 야외 조형물을 지워 버린 날. 겨레의 기억은 전시관 안으로 물러나 있다. "
                        + "근대가 남긴 기록과 옛사람이 쓰던 살림살이, 지붕 아래 남은 두 조각을 차례로 이어 붙여라.",
                "천안·아산",
                Difficulty.EASY,
                330,
                IndoorType.INDOOR,
                union(ALL_WEATHER, CourseTag.HISTORY),
                List.of(
                        new SpotDef("INDEPENDENCE_HALL", (short) 1,
                                "지붕 아래에서 시작한다. 겨레의 집을 지나 전시관을 차례로 돌면, 오늘은 한 번도 비를 맞지 않는다.",
                                BigDecimal.valueOf(36.7826), BigDecimal.valueOf(127.2226), 250, true,
                                List.of(
                                        choiceQuiz("독립기념관이 문을 연 해는?", "광복절에 개관했습니다.",
                                                choice("①", "1975년", false),
                                                choice("②", "1987년", true),
                                                choice("③", "1995년", false),
                                                choice("④", "2005년", false)),
                                        textQuiz("독립기념관의 중심 건물로, 겨레의 얼을 상징하는 대형 전시관의 이름은?", "겨레의 집"),
                                        photo("전시관 내부에서 팀 전원 인증샷"))),
                        new SpotDef("ONYANG_FOLK_MUSEUM", (short) 2,
                                "[최종] 마지막 조각은 사람이 손에 쥐고 살던 물건들 사이에 있다. 비 오는 날일수록 그 방은 더 그럴듯해진다.",
                                BigDecimal.valueOf(36.7885), BigDecimal.valueOf(127.0044), 150, true,
                                List.of(
                                        oxQuiz("온양은 조선시대 임금들이 찾던 온천으로 이름난 곳이다", true, null),
                                        textQuiz("온양민속박물관이 있는 충남의 시는?", "아산시"),
                                        photo("전시실 안 민속품 앞에서 팀 전원 인증샷 — 실내 탐사 완료!")))
                ));
    }

    private static Set<CourseTag> union(Set<CourseTag> base, CourseTag extra) {
        return java.util.stream.Stream.concat(base.stream(), java.util.stream.Stream.of(extra))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
