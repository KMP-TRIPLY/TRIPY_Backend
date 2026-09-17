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
 * 충남 지역별 당일치기 코스 콘텐츠.
 * 게임방 하나가 하루 안에 끝나도록 모두 당일치기 단위로 끊는다 —
 * 여러 날 여행은 날마다 다른 지역 코스로 방을 새로 파면 되고, 방을 밤새 켜둘 일이 없다.
 */
final class ChungnamDayTripStories {

    static final String SEOSAN_TAEAN = "하루 만에 서해 낙조를 훔쳐라";
    static final String YESAN_HONGSEONG = "내포의 보물지도";
    static final String BORYEONG = "바다가 갈라지는 시간";
    static final String CHEONAN_ASAN = "돌담 아래 묻힌 겨레의 기억";
    static final String GONGJU = "웅진, 왕이 숨긴 하루";
    static final String BUYEO = "사비의 마지막 하루";
    static final String SEOCHEON = "금강 하구의 생태 탐사대";

    private ChungnamDayTripStories() {
    }

    static List<StoryDef> stories() {
        return List.of(seosanTaean(), yesanHongseong(), boryeong(),
                cheonanAsan(), gongju(), buyeo(), seocheon());
    }

    /** 서산·태안 — 내륙 유적에서 시작해 서해 낙조로 끝난다. */
    private static StoryDef seosanTaean() {
        return new StoryDef(
                SEOSAN_TAEAN,
                "해 지기 전까지 서산과 태안에 흩어진 '빛의 조각' 네 개를 모아야 한다. "
                        + "마지막 조각은 꽃지 앞바다의 노을 속에 있다. 해가 바다에 닿기 전에 도착하라.",
                "서산·태안",
                Difficulty.EASY,
                480,
                IndoorType.OUTDOOR,
                Set.of(CourseTag.SUNSET, CourseTag.PHOTO_SPOT, CourseTag.HISTORY, CourseTag.NATURE),
                List.of(
                        new SpotDef("HAEMI_EUPSEONG", (short) 1,
                                "첫 번째 빛의 조각은 조선 수군과 병마가 드나들던 성문 아래 숨어 있다. "
                                        + "옹성을 지나 성안으로 들어서면 단서가 보인다. (해미읍성 남문 반경 50m 이내 진입)",
                                BigDecimal.valueOf(36.7060), BigDecimal.valueOf(126.5563), 50,
                                List.of(
                                        choiceQuiz("해미읍성에 주둔했던 조선시대 군사 기관은?", "바다가 아니라 육군입니다.",
                                                choice("①", "충청수군절도사영", false),
                                                choice("②", "충청병마절도사영", true),
                                                choice("③", "삼도수군통제영", false),
                                                choice("④", "훈련도감", false)),
                                        textQuiz("해미읍성의 정문인 남문의 이름은?", "진남문"),
                                        photo("진남문 현판이 보이도록 팀 전원 인증샷"))),
                        new SpotDef("YONGHYEONRI_MAAE", (short) 2,
                                "[이동] 내륙 산길로 들어가라. 두 번째 조각은 바위에 새겨진 미소가 지키고 있다. "
                                        + "햇빛의 각도에 따라 표정이 바뀌는 그 얼굴을 직접 확인하라.",
                                BigDecimal.valueOf(36.7397), BigDecimal.valueOf(126.5893), 100,
                                List.of(
                                        textQuiz("서산 용현리 마애여래삼존상의 별명은? (다섯 글자)", "백제의 미소"),
                                        oxQuiz("이 마애삼존상은 백제 시대에 만들어진 불상이다", true,
                                                "6~7세기 백제 후기 작품으로 봅니다."),
                                        photo("삼존상의 미소를 따라 하는 표정으로 팀 전원 인증샷"))),
                        new SpotDef("GANWOLAM", (short) 3,
                                "[이동] 다시 바다 쪽으로. 세 번째 조각은 하루에 두 번만 길이 열리는 작은 암자에 있다. "
                                        + "물때를 놓치면 조각도 놓친다.",
                                BigDecimal.valueOf(36.7370), BigDecimal.valueOf(126.3530), 80,
                                List.of(
                                        textQuiz("간월암을 창건했다고 전해지는, 조선 건국을 도운 승려는?", "무학대사"),
                                        oxQuiz("간월암은 밀물 때 섬이 되고 썰물 때 걸어서 들어갈 수 있다", true, null),
                                        photo("간월암과 바닷길이 함께 나오도록 팀 전원 인증샷"))),
                        new SpotDef("KKOTJI_BEACH", (short) 4,
                                "[최종] 마지막 조각은 노을 그 자체다. 두 개의 바위 사이로 해가 내려앉는 순간, 빛의 조각이 완성된다.",
                                BigDecimal.valueOf(36.4433), BigDecimal.valueOf(126.3305), 200,
                                List.of(
                                        choiceQuiz("꽃지해수욕장 낙조로 유명한 두 바위의 이름은?", null,
                                                choice("①", "형제바위", false),
                                                choice("②", "할미바위·할아비바위", true),
                                                choice("③", "촛대바위", false),
                                                choice("④", "코끼리바위", false)),
                                        textQuiz("꽃지해수욕장이 자리한 태안의 섬 이름은?", "안면도"),
                                        photo("할미·할아비바위를 배경으로 노을 실루엣 단체샷 — 원정 완료!")))
                ));
    }

    /** 예산·홍성 — 산사에서 저수지를 지나 포구로. */
    private static StoryDef yesanHongseong() {
        return new StoryDef(
                YESAN_HONGSEONG,
                "내포 땅 어딘가에 조선 수군이 숨긴 보물이 있다. 지도는 세 조각으로 찢겨 산사와 저수지와 포구에 흩어졌다. "
                        + "해 지기 전에 모두 모아 남당항에서 맞춰라.",
                "예산·홍성",
                Difficulty.EASY,
                420,
                IndoorType.MIXED,
                Set.of(CourseTag.HISTORY, CourseTag.NATURE, CourseTag.FOOD),
                List.of(
                        new SpotDef("SUDEOKSA", (short) 1,
                                "지도 1조각. 덕숭산 자락의 천년 고찰에서 시작한다. "
                                        + "우리나라에서 가장 오래된 목조 건물 중 하나가 첫 단서를 품고 있다.",
                                BigDecimal.valueOf(36.6633), BigDecimal.valueOf(126.6169), 150,
                                List.of(
                                        choiceQuiz("수덕사 대웅전이 지어진 시대는?", "건립 연도(1308년)가 확인된 몇 안 되는 목조 건축입니다.",
                                                choice("①", "통일신라", false),
                                                choice("②", "고려", true),
                                                choice("③", "조선 전기", false),
                                                choice("④", "조선 후기", false)),
                                        textQuiz("수덕사가 자리한 산의 이름은?", "덕숭산"),
                                        photo("대웅전 앞 계단에서 팀 전원 인증샷"))),
                        new SpotDef("YEDANGHO_BRIDGE", (short) 2,
                                "지도 2조각. 물 위에 놓인 긴 다리를 건너야 한다. 흔들리는 만큼 단서도 흔들린다.",
                                BigDecimal.valueOf(36.6864), BigDecimal.valueOf(126.7718), 150,
                                List.of(
                                        oxQuiz("예당호는 농업용수를 대기 위해 만든 저수지다", true, null),
                                        choiceQuiz("예당호 출렁다리의 길이는?", null,
                                                choice("①", "202m", false),
                                                choice("②", "402m", true),
                                                choice("③", "602m", false),
                                                choice("④", "802m", false)),
                                        photo("출렁다리 한가운데에서 점프샷"))),
                        new SpotDef("NAMDANGHANG", (short) 3,
                                "[최종] 지도 3조각. 포구의 상인만 아는 암호가 있다. 제철 해산물의 이름이 곧 암호다.",
                                BigDecimal.valueOf(36.5316), BigDecimal.valueOf(126.4772), 200,
                                List.of(
                                        choiceQuiz("남당항의 가을(9~11월) 대표 제철 수산물은?", null,
                                                choice("①", "대하", true),
                                                choice("②", "과메기", false),
                                                choice("③", "멍게", false),
                                                choice("④", "가리비", false)),
                                        textQuiz("남당항에서 겨울~초봄에 유명한, 샤브샤브로 먹는 조개는?", "새조개"),
                                        photo("팀이 함께 먹은 메뉴와 인증샷 — 보물 획득!")))
                ));
    }

    /** 보령 — 수군 진영에서 갯벌, 그리고 바닷길. */
    private static StoryDef boryeong() {
        return new StoryDef(
                BORYEONG,
                "한 달에 며칠, 무창포 앞바다가 갈라져 섬까지 길이 열린다. "
                        + "그 길 끝에 닿으려면 먼저 보령의 바다를 지켜온 세 곳의 열쇠를 모아야 한다. 물때표를 확인하고 출발하라.",
                "보령",
                Difficulty.NORMAL,
                420,
                IndoorType.OUTDOOR,
                Set.of(CourseTag.NATURE, CourseTag.PHOTO_SPOT, CourseTag.FOOD),
                List.of(
                        new SpotDef("CHUNGCHEONG_SUYEONGSEONG", (short) 1,
                                "첫 열쇠는 서해를 지키던 수군의 본영에 있다. 천수만이 한눈에 보이는 자리에 단서가 새겨져 있다.",
                                BigDecimal.valueOf(36.3673), BigDecimal.valueOf(126.5117), 120,
                                List.of(
                                        choiceQuiz("충청수영성은 조선시대 어떤 군의 사령부였나?", null,
                                                choice("①", "수군", true),
                                                choice("②", "기병", false),
                                                choice("③", "포도청", false),
                                                choice("④", "봉수군", false)),
                                        textQuiz("충청수영성에 남아 있는 아치형 성문의 이름은?", "망화문"),
                                        photo("성곽 위에서 천수만이 보이는 방향으로 팀 전원 인증샷"))),
                        new SpotDef("DAECHEON_BEACH", (short) 2,
                                "두 번째 열쇠. 여름이면 온 거리가 진흙으로 뒤덮이는 해변이다. 진흙 속에 단서가 있다.",
                                BigDecimal.valueOf(36.3097), BigDecimal.valueOf(126.5133), 200,
                                List.of(
                                        choiceQuiz("대천해수욕장에서 매년 여름 열리는 대표 축제는?", null,
                                                choice("①", "보령머드축제", true),
                                                choice("②", "보령불꽃축제", false),
                                                choice("③", "대천물총축제", false),
                                                choice("④", "서해모래축제", false)),
                                        oxQuiz("보령머드축제의 머드는 보령 앞바다 갯벌에서 채취한 진흙으로 만든다", true, null),
                                        photo("머드광장 조형물 앞에서 팀 전원 인증샷"))),
                        new SpotDef("MUCHANGPO", (short) 3,
                                "[최종] 마지막 열쇠. 바다가 갈라지는 시각에 맞춰 도착했는가? 그 길 위에서 임무를 끝낸다.",
                                BigDecimal.valueOf(36.2461), BigDecimal.valueOf(126.5238), 200,
                                List.of(
                                        textQuiz("바닷물이 빠지며 길이 드러나게 만드는, 밀물과 썰물의 높이 차를 뜻하는 말은?",
                                                "조수간만의 차"),
                                        oxQuiz("무창포는 '신비의 바닷길'로 알려진 대표적인 해수욕장이다", true, null),
                                        photo("바닷길(또는 석대도) 방향을 배경으로 단체샷 — 임무 완료!")))
                ));
    }

    /** 천안·아산 — 근대의 기억과 돌담 마을. */
    private static StoryDef cheonanAsan() {
        return new StoryDef(
                CHEONAN_ASAN,
                "누군가 겨레의 기억 세 조각을 흩어 놓았다. 흑성산 아래 전시관, 충무공의 사당, 그리고 돌담 마을에 하나씩. "
                        + "세 조각을 모두 찾아 오늘 안에 맞춰라.",
                "천안·아산",
                Difficulty.EASY,
                400,
                IndoorType.MIXED,
                Set.of(CourseTag.HISTORY, CourseTag.MUSEUM, CourseTag.KID_FRIENDLY),
                List.of(
                        new SpotDef("INDEPENDENCE_HALL", (short) 1,
                                "첫 조각은 가장 가까운 시대에 있다. 흑성산 아래 겨레의 집에서 시작한다.",
                                BigDecimal.valueOf(36.7826), BigDecimal.valueOf(127.2226), 250, true,
                                List.of(
                                        choiceQuiz("독립기념관이 문을 연 해는?", "광복절에 개관했습니다.",
                                                choice("①", "1975년", false),
                                                choice("②", "1987년", true),
                                                choice("③", "1995년", false),
                                                choice("④", "2005년", false)),
                                        textQuiz("독립기념관의 중심 건물로, 겨레의 얼을 상징하는 대형 전시관의 이름은?", "겨레의 집"),
                                        photo("겨레의 집을 배경으로 팀 전원 인증샷"))),
                        new SpotDef("HYEONCHUNGSA", (short) 2,
                                "[이동] 두 번째 조각은 충무공을 모신 사당에 있다. 그가 무과에 급제하기 전까지 살았던 땅이다.",
                                BigDecimal.valueOf(36.7920), BigDecimal.valueOf(126.9964), 200,
                                List.of(
                                        choiceQuiz("현충사에 모셔진 인물은?", null,
                                                choice("①", "이순신", true),
                                                choice("②", "김유신", false),
                                                choice("③", "강감찬", false),
                                                choice("④", "권율", false)),
                                        textQuiz("이순신 장군이 임진왜란 7년을 기록한 일기의 이름은?", "난중일기"),
                                        photo("본전 앞에서 팀 전원 인증샷"))),
                        new SpotDef("OEAM_VILLAGE", (short) 3,
                                "[최종] 마지막 조각은 돌담 사이에 있다. 수백 년간 사람이 살아온 마을에서 기억을 완성하라.",
                                BigDecimal.valueOf(36.7346), BigDecimal.valueOf(126.9399), 200,
                                List.of(
                                        oxQuiz("외암마을은 지금도 주민이 살고 있는 민속마을이다", true, null),
                                        textQuiz("외암마을 뒤편에 솟아 마을의 배경이 되는 산의 이름은?", "설화산"),
                                        photo("돌담길에서 팀 전원 인증샷 — 기억 복원 완료!")))
                ));
    }

    /** 공주 — 웅진 도읍기의 하루. */
    private static StoryDef gongju() {
        return new StoryDef(
                GONGJU,
                "백제가 두 번째 도읍으로 삼은 웅진. 왕이 하루 동안 숨겨둔 세 가지를 찾아라. "
                        + "성벽 위, 무덤 속, 그리고 산사에 하나씩이다.",
                "공주",
                Difficulty.NORMAL,
                420,
                IndoorType.MIXED,
                Set.of(CourseTag.HISTORY, CourseTag.MUSEUM),
                List.of(
                        new SpotDef("GONGSANSEONG", (short) 1,
                                "왕이 처음 숨긴 것은 도읍의 이름 그 자체다. 금강을 내려다보는 성벽 위에서 찾아라. "
                                        + "(공산성 쌍수정 반경 100m 이내 진입)",
                                BigDecimal.valueOf(36.4585), BigDecimal.valueOf(127.1229), 100,
                                List.of(
                                        choiceQuiz("공산성이 지키던 백제의 두 번째 도읍 이름은?", null,
                                                choice("①", "한성", false),
                                                choice("②", "웅진", true),
                                                choice("③", "사비", false),
                                                choice("④", "국내성", false)),
                                        oxQuiz("공산성은 조선시대에 돌로 다시 쌓은 구간이 있다", true, null),
                                        photo("성벽 위에서 금강이 보이는 방향으로 팀 전원 인증샷"))),
                        new SpotDef("MURYEONG_TOMB", (short) 2,
                                "[이동] 두 번째로 숨긴 것은 무덤 안에 있다. 도굴되지 않은 채 발견되어 백제사를 통째로 바꾼 왕릉이다.",
                                BigDecimal.valueOf(36.4620), BigDecimal.valueOf(127.1145), 150, true,
                                List.of(
                                        choiceQuiz("무령왕릉이 세상에 알려진(발굴된) 해는?", null,
                                                choice("①", "1948년", false),
                                                choice("②", "1971년", true),
                                                choice("③", "1985년", false),
                                                choice("④", "1999년", false)),
                                        textQuiz("무령왕릉 입구에서 무덤을 지키던, 돌로 만든 상상의 동물 이름은?", "진묘수"),
                                        photo("왕릉원 능선을 배경으로 팀 전원 인증샷"))),
                        new SpotDef("MAGOKSA", (short) 3,
                                "[최종] 마지막은 속세를 떠난 자리에 있다. 산과 물이 태극처럼 감아 도는 천년 고찰에서 하루를 끝낸다.",
                                BigDecimal.valueOf(36.5405), BigDecimal.valueOf(127.0132), 200,
                                List.of(
                                        textQuiz("마곡사에서 김구 선생이 은거할 당시 사용한 법명은?", "원종"),
                                        oxQuiz("마곡사는 유네스코 세계유산 '산사, 한국의 산지승원'에 포함된다", true, null),
                                        photo("대웅보전 현판 앞에서 팀 전원 인증샷 — 왕의 하루 완료!")))
                ));
    }

    /** 부여 — 사비 도읍의 마지막 장면. */
    private static StoryDef buyeo() {
        return new StoryDef(
                BUYEO,
                "백제 마지막 도읍 사비의 하루를 되짚는다. 절벽에서 시작해 재현된 왕궁을 지나 연못에서 끝나는, "
                        + "왕국의 마지막 동선을 그대로 따라가라.",
                "부여",
                Difficulty.NORMAL,
                420,
                IndoorType.MIXED,
                Set.of(CourseTag.HISTORY, CourseTag.PHOTO_SPOT),
                List.of(
                        new SpotDef("BUSOSANSEONG", (short) 1,
                                "왕궁의 뒷산부터 오른다. 절벽 끝에서 왕국의 마지막 장면을 마주하게 된다.",
                                BigDecimal.valueOf(36.2870), BigDecimal.valueOf(126.9128), 120,
                                List.of(
                                        choiceQuiz("낙화암 아래 백마강 가에 자리한 암자의 이름은?", null,
                                                choice("①", "고란사", true),
                                                choice("②", "보문사", false),
                                                choice("③", "신흥사", false),
                                                choice("④", "부석사", false)),
                                        textQuiz("고란사 뒤 바위틈에서 자라는 것으로 유명한 희귀 식물의 이름은?", "고란초"),
                                        photo("낙화암 백화정에서 백마강을 배경으로 팀 전원 인증샷"))),
                        new SpotDef("BAEKJE_CULTURE_LAND", (short) 2,
                                "[이동] 강을 건너면 사라진 왕궁이 다시 세워져 있다. 기록만 남은 건물이 실제 크기로 서 있는 곳이다.",
                                BigDecimal.valueOf(36.3151), BigDecimal.valueOf(126.8905), 250,
                                List.of(
                                        choiceQuiz("백제문화단지의 사비궁이 재현한 백제의 도읍은?", null,
                                                choice("①", "한성", false),
                                                choice("②", "웅진", false),
                                                choice("③", "사비", true),
                                                choice("④", "위례", false)),
                                        oxQuiz("백제문화단지의 능사 5층 목탑은 발굴 조사 결과를 바탕으로 재현했다", true, null),
                                        photo("능사 5층 목탑 앞에서 팀 전원 인증샷"))),
                        new SpotDef("GUNGNAMJI", (short) 3,
                                "[최종] 마지막은 왕궁의 정원이다. 연꽃이 덮인 연못 한가운데 섬에서 하루를 끝낸다.",
                                BigDecimal.valueOf(36.2681), BigDecimal.valueOf(126.9209), 200,
                                List.of(
                                        choiceQuiz("궁남지는 우리나라 최초의 무엇으로 알려져 있나?", null,
                                                choice("①", "저수지", false),
                                                choice("②", "인공연못", true),
                                                choice("③", "왕궁정원", false),
                                                choice("④", "인공섬", false)),
                                        textQuiz("궁남지 인공섬 위에 세워진 정자의 이름은?", "포룡정"),
                                        photo("포룡정 다리 위에서 연못을 배경으로 단체샷 — 사비의 하루 완료!")))
                ));
    }

    /** 서천 — 반나절 생태 코스. */
    private static StoryDef seocheon() {
        return new StoryDef(
                SEOCHEON,
                "금강이 바다로 풀리는 자리에 탐사 구역 두 곳이 있다. 문 하나를 지날 때마다 기후가 바뀌는 "
                        + "건물을 먼저 통과하고, 소나무 숲 위로 난 길을 걸어 서해와 마주하는 순간 탐사가 끝난다. "
                        + "해가 남아 있는 반나절이면 된다.",
                "서천",
                Difficulty.EASY,
                300,
                IndoorType.MIXED,
                Set.of(CourseTag.NATURE, CourseTag.MUSEUM, CourseTag.KID_FRIENDLY, CourseTag.LIGHT_WALK),
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
                                        photo("에코리움 온실 안에서 팀 전원 인증샷"))),
                        new SpotDef("JANGHANG_SKYWALK", (short) 2,
                                "[최종] 탐사 2구역. 숲을 아래로 두고 걷는다. 전망대 끝에서 서해를 보면 탐사가 끝난다.",
                                BigDecimal.valueOf(36.0030), BigDecimal.valueOf(126.6857), 150,
                                List.of(
                                        choiceQuiz("장항스카이워크가 지나가는 송림숲의 주된 나무는?", null,
                                                choice("①", "해송(곰솔)", true),
                                                choice("②", "은행나무", false),
                                                choice("③", "대나무", false),
                                                choice("④", "자작나무", false)),
                                        oxQuiz("장항스카이워크는 소나무 숲 위를 걸어서 지나가는 구조물이다", true, null),
                                        photo("전망대에서 서해를 배경으로 단체샷 — 탐사 완료!")))
                ));
    }
}
