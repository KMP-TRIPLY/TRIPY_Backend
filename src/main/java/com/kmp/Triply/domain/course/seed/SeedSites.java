package com.kmp.Triply.domain.course.seed;

import com.kmp.Triply.domain.tourism.entity.SpotCategory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 시드 코스가 참조하는 충남 관광지 목록.
 * 좌표는 미션 지오펜스의 기준이 되므로, 현장 검증 후 정밀 좌표로 보정하는 것을 전제로 한다.
 */
final class SeedSites {

    private SeedSites() {
    }

    static List<SiteDef> all() {
        return List.of(
                // --- 공주·부여(백제 역사 스토리 A/B/C 공용) ---
                site("GONGSANSEONG", "공산성", SpotCategory.HERITAGE,
                        "충청남도 공주시 금성동 65-3", 36.4585, 127.1229),
                site("MAGOKSA", "마곡사", SpotCategory.HERITAGE,
                        "충청남도 공주시 사곡면 마곡사로 966", 36.5405, 127.0132),
                site("JEONGNIMSAJI", "정림사지", SpotCategory.HERITAGE,
                        "충청남도 부여군 부여읍 정림로 83", 36.2762, 126.9098),
                site("GUNGNAMJI", "궁남지", SpotCategory.HERITAGE,
                        "충청남도 부여군 부여읍 궁남로 52", 36.2681, 126.9209),

                // --- 서산·태안(당일치기) ---
                site("HAEMI_EUPSEONG", "해미읍성", SpotCategory.HERITAGE,
                        "충청남도 서산시 해미면 남문2로 143", 36.7060, 126.5563),
                site("YONGHYEONRI_MAAE", "서산 용현리 마애여래삼존상", SpotCategory.HERITAGE,
                        "충청남도 서산시 운산면 마애삼존불길 65", 36.7397, 126.5893),
                site("GANWOLAM", "간월암", SpotCategory.HERITAGE,
                        "충청남도 서산시 부석면 간월도1길 119-29", 36.7370, 126.3530),
                site("KKOTJI_BEACH", "꽃지해수욕장", SpotCategory.NATURE,
                        "충청남도 태안군 안면읍 꽃지해안로 400", 36.4433, 126.3305),

                // --- 예산·홍성·보령(당일치기) ---
                site("SUDEOKSA", "수덕사", SpotCategory.HERITAGE,
                        "충청남도 예산군 덕산면 수덕사안길 79", 36.6633, 126.6169),
                site("YEDANGHO_BRIDGE", "예당호 출렁다리", SpotCategory.NATURE,
                        "충청남도 예산군 응봉면 예당관광로 178", 36.6864, 126.7718),
                site("NAMDANGHANG", "남당항", SpotCategory.FOOD,
                        "충청남도 홍성군 서부면 남당항로 213", 36.5316, 126.4772),
                site("CHUNGCHEONG_SUYEONGSEONG", "보령 충청수영성", SpotCategory.HERITAGE,
                        "충청남도 보령시 오천면 소성리 931-1", 36.3673, 126.5117),
                site("DAECHEON_BEACH", "대천해수욕장", SpotCategory.NATURE,
                        "충청남도 보령시 머드로 123", 36.3097, 126.5133),
                site("MUCHANGPO", "무창포해수욕장", SpotCategory.NATURE,
                        "충청남도 보령시 웅천읍 열린바다1길 10", 36.2461, 126.5238),

                // --- 천안·아산·공주·부여·서천(당일치기) ---
                site("INDEPENDENCE_HALL", "독립기념관", SpotCategory.HERITAGE,
                        "충청남도 천안시 동남구 목천읍 독립기념관로 1", 36.7826, 127.2226),
                site("HYEONCHUNGSA", "현충사", SpotCategory.HERITAGE,
                        "충청남도 아산시 염치읍 현충사길 126", 36.7920, 126.9964),
                site("OEAM_VILLAGE", "아산 외암마을", SpotCategory.HERITAGE,
                        "충청남도 아산시 송악면 외암민속길 42-15", 36.7346, 126.9399),
                site("MURYEONG_TOMB", "공주 무령왕릉과 왕릉원", SpotCategory.HERITAGE,
                        "충청남도 공주시 왕릉로 37", 36.4620, 127.1145),
                site("BUSOSANSEONG", "부소산성 낙화암", SpotCategory.HERITAGE,
                        "충청남도 부여군 부여읍 부소로 31", 36.2870, 126.9128),
                site("BAEKJE_CULTURE_LAND", "백제문화단지", SpotCategory.HERITAGE,
                        "충청남도 부여군 규암면 백제문로 455", 36.3151, 126.8905),
                site("ECOREUM", "국립생태원", SpotCategory.NATURE,
                        "충청남도 서천군 마서면 금강로 1210", 36.0243, 126.7205),
                site("JANGHANG_SKYWALK", "장항스카이워크", SpotCategory.NATURE,
                        "충청남도 서천군 장항읍 장항산단로34번길 654", 36.0030, 126.6857),

                // --- 실내(비 오는 날) 코스 ---
                site("BUYEO_MUSEUM", "국립부여박물관", SpotCategory.HERITAGE,
                        "충청남도 부여군 부여읍 금성로 5", 36.2758, 126.9147),
                site("JEONGNIMSAJI_MUSEUM", "정림사지박물관", SpotCategory.HERITAGE,
                        "충청남도 부여군 부여읍 정림로 83", 36.2768, 126.9089),
                site("BAEKJE_HISTORY_MUSEUM", "백제역사문화관", SpotCategory.HERITAGE,
                        "충청남도 부여군 규암면 백제문로 455", 36.3138, 126.8898),
                site("MARINE_BIO_RESOURCES", "국립해양생물자원관", SpotCategory.NATURE,
                        "충청남도 서천군 장항읍 장산로 101-75", 36.0056, 126.6875),
                site("ONYANG_FOLK_MUSEUM", "온양민속박물관", SpotCategory.HERITAGE,
                        "충청남도 아산시 충무로 123", 36.7885, 127.0044));
    }

    private static SiteDef site(String key, String name, SpotCategory category,
                                String address, double lat, double lng) {
        return new SiteDef(key, name, category, address, BigDecimal.valueOf(lat), BigDecimal.valueOf(lng));
    }
}
