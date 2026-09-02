package com.kmp.Triply.domain.course;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 국가행정표준 시도 코드. 관광공사 API 의 areaCd 와 같은 기준이다.
 *
 * <p>클라이언트는 시도 코드를 모르므로 코스 생성 시 도시 이름만 보낸다.
 * {@link #resolve(String)} 가 그 이름으로 코드를 찾는다.
 *
 * <p>못 찾으면 빈 값을 돌려준다. 틀린 코드를 넣는 것보다 400 으로 되돌려
 * 요청자가 regionCode 를 직접 지정하게 하는 편이 안전하다.
 */
public final class RegionCode {

    /**
     * 시도 코드 | 시도 이름 | 검색 키워드 (시도 약칭 + 시군구, 접미사 시/군/구 는 뗀다)
     *
     * <p>키워드는 "포함"으로 매칭하므로 "공주시"·"공주·부여" 같은 입력도 잡힌다.
     * 시도 약칭을 함께 넣어둔 덕분에 "경기도 광주시" 처럼 두 시도 키워드가
     * 섞인 입력은 모호한 것으로 처리되어 코드가 정해지지 않는다.
     *
     * <p>여러 시도에 같은 이름이 있는 시군구(고성군 등)는 자동으로 제외된다.
     * 목록에 없는 도시는 매칭되지 않으므로, 필요해지면 해당 줄에 이름만 추가하면 된다.
     */
    private static final String[][] REGIONS = {
            {"11", "서울특별시", "서울"},
            {"26", "부산광역시", "부산 기장"},
            {"27", "대구광역시", "대구 달성 군위"},
            {"28", "인천광역시", "인천 강화 옹진"},
            {"29", "광주광역시", "광주"},
            {"30", "대전광역시", "대전"},
            {"31", "울산광역시", "울산 울주"},
            {"36", "세종특별자치시", "세종"},
            {"41", "경기도", "경기 수원 성남 의정부 안양 부천 광명 평택 동두천 안산 고양 과천 구리 "
                    + "남양주 오산 시흥 군포 의왕 하남 용인 파주 이천 안성 김포 화성 양주 포천 여주 연천 가평 양평"},
            {"43", "충청북도", "충북 청주 충주 제천 보은 옥천 영동 증평 진천 괴산 음성 단양"},
            {"44", "충청남도", "충남 천안 공주 보령 아산 서산 논산 계룡 당진 금산 부여 서천 청양 홍성 예산 태안"},
            {"46", "전라남도", "전남 목포 여수 순천 나주 광양 담양 곡성 구례 고흥 보성 화순 장흥 강진 "
                    + "해남 영암 무안 함평 영광 장성 완도 진도 신안"},
            {"47", "경상북도", "경북 포항 경주 김천 안동 구미 영주 영천 상주 문경 경산 의성 청송 영양 "
                    + "영덕 청도 고령 성주 칠곡 예천 봉화 울진 울릉"},
            {"48", "경상남도", "경남 창원 진주 통영 사천 김해 밀양 거제 양산 의령 함안 창녕 남해 하동 산청 함양 거창 합천"},
            {"50", "제주특별자치도", "제주 서귀포"},
            {"51", "강원특별자치도", "강원 춘천 원주 강릉 동해 태백 속초 삼척 홍천 횡성 영월 평창 정선 철원 화천 양구 인제 양양"},
            {"52", "전북특별자치도", "전북 전주 군산 익산 정읍 남원 김제 완주 진안 무주 장수 임실 순창 고창 부안"},
    };

    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    private static final Map<String, String> CODE_BY_KEYWORD = new HashMap<>();

    static {
        Set<String> duplicated = new HashSet<>();
        for (String[] region : REGIONS) {
            String code = region[0];
            NAMES.put(code, region[1]);
            for (String keyword : region[2].split(" ")) {
                String previous = CODE_BY_KEYWORD.put(keyword, code);
                if (previous != null && !previous.equals(code)) {
                    duplicated.add(keyword);
                }
            }
        }
        // 두 시도에 같은 이름이 있으면 어느 쪽인지 정할 수 없으니 아예 쓰지 않는다.
        duplicated.forEach(CODE_BY_KEYWORD::remove);
    }

    private RegionCode() {
    }

    /** 도시 이름으로 시도 코드를 찾는다. 못 찾거나 여러 시도가 섞여 있으면 빈 값. */
    public static Optional<String> resolve(String city) {
        if (city == null || city.isBlank()) {
            return Optional.empty();
        }

        String text = city.replaceAll("\\s", "");
        String found = null;
        for (Map.Entry<String, String> entry : CODE_BY_KEYWORD.entrySet()) {
            if (!text.contains(entry.getKey())) {
                continue;
            }
            if (found != null && !found.equals(entry.getValue())) {
                return Optional.empty();
            }
            found = entry.getValue();
        }
        return Optional.ofNullable(found);
    }

    /** 시도 코드의 이름. 모르는 코드면 코드를 그대로 돌려준다. */
    public static String nameOf(String regionCode) {
        return NAMES.getOrDefault(regionCode, regionCode);
    }

    public static boolean exists(String regionCode) {
        return NAMES.containsKey(regionCode);
    }
}
