package com.kmp.Triply.domain.tourism.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스팟 사진은 좌표 주변에서 이름이 같은 곳을 찾아 붙인다.
 * 거리순 1 위를 쓰면 다른 장소 사진이 붙으므로(국립공주박물관 좌표 222m 지점이 선화당)
 * 이름 비교가 유일한 방어선이다.
 */
class SpotNameMatchTest {

    @Test
    void 괄호_안_부가설명을_떼고_비교한다() {
        // 관광정보 쪽 표기: "선화당(공주)", "공주 무령왕릉과 왕릉원[유네스코 세계유산]"
        assertThat(TourismApiServiceImpl.normalizeSpotName("선화당(공주)")).isEqualTo("선화당");
        assertThat(TourismApiServiceImpl.normalizeSpotName("공주 무령왕릉과 왕릉원[유네스코 세계유산]"))
                .isEqualTo("공주무령왕릉과왕릉원");
    }

    @Test
    void 공백_차이는_같은_이름으로_본다() {
        assertThat(TourismApiServiceImpl.normalizeSpotName("공주 고마나루"))
                .isEqualTo(TourismApiServiceImpl.normalizeSpotName("공주고마나루"));
    }

    @Test
    void 다른_장소는_같아지지_않는다() {
        // 이 둘이 같아지면 게스트하우스 사진이 공산성에 붙는다
        assertThat(TourismApiServiceImpl.normalizeSpotName("공산성"))
                .isNotEqualTo(TourismApiServiceImpl.normalizeSpotName("공주공산성게스트 하우스"));
        assertThat(TourismApiServiceImpl.normalizeSpotName("국립공주박물관"))
                .isNotEqualTo(TourismApiServiceImpl.normalizeSpotName("선화당(공주)"));
    }

    @Test
    void 빈_값도_다룬다() {
        assertThat(TourismApiServiceImpl.normalizeSpotName(null)).isEmpty();
        assertThat(TourismApiServiceImpl.normalizeSpotName("")).isEmpty();
    }

    // ---- 접두 일치 ----
    // 관광정보에 스팟 이름 그대로 없는 경우가 많다. 실측 12곳 중 완전일치는 2곳뿐이었고
    // 접두를 허용해 4곳이 됐다.

    @Test
    void 관광정보_이름이_스팟_이름으로_시작하면_같은_곳으로_본다() {
        assertThat(TourismApiServiceImpl.isPrefixMatch("청남대", "청남대가을축제")).isTrue();
        assertThat(TourismApiServiceImpl.isPrefixMatch("대전역", "대전역동광장")).isTrue();
    }

    @Test
    void 이름_안에_들어만_있는_것은_받지_않는다() {
        // 이 둘을 받으면 게스트하우스·화장품가게 사진이 사적에 붙는다
        assertThat(TourismApiServiceImpl.isPrefixMatch("공산성", "공주공산성게스트하우스")).isFalse();
        assertThat(TourismApiServiceImpl.isPrefixMatch("대전역", "올리브영대전역점")).isFalse();
    }

    @Test
    void 짧은_이름은_접두로_보지_않는다() {
        // 두 글자는 아무 데나 걸린다
        assertThat(TourismApiServiceImpl.isPrefixMatch("공주", "공주산성시장")).isFalse();
        assertThat(TourismApiServiceImpl.isPrefixMatch("", "무엇이든")).isFalse();
    }

    @Test
    void 세_글자_이상이면_접두로_본다() {
        assertThat(TourismApiServiceImpl.isPrefixMatch("고마나루", "고마나루1999")).isTrue();
    }
}
