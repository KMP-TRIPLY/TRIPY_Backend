package com.kmp.Triply.domain.tourism.service;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터랩 순위는 관광지 순위가 아니라 방문자 수 순위다.
 * 그래서 호텔·역·백화점이 시군구 1위로 올라와 추천 명소 자리를 차지한다.
 * 운영 데이터에서 실제로 rank 1 에 대전역·롯데백화점이 있었다.
 */
class RecommendableSpotTest {

    private static RecommendationResponse spot(String title, String categoryMiddle) {
        return RecommendationResponse.builder()
                .contentId(title)
                .title(title)
                .categoryMiddle(categoryMiddle)
                .build();
    }

    @Test
    void 숙박은_추천에서_뺀다() {
        // 운영 1004 건 중 333 건이 숙박이었다
        assertThat(TourismApiServiceImpl.isRecommendable(spot("포레스트리솜", "숙박"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("토요코인호텔/대전정부청사앞점", "숙박"))).isFalse();
    }

    @Test
    void 역은_추천에서_뺀다() {
        // 역은 청남대와 같은 '기타관광' 이라 분류로는 갈라지지 않는다
        assertThat(TourismApiServiceImpl.isRecommendable(spot("대전역", "기타관광"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("천안아산역", "기타관광"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("온양온천역", "기타관광"))).isFalse();
    }

    @Test
    void 백화점_아울렛_도매시장은_추천에서_뺀다() {
        // 백화점은 재래시장과 같은 '쇼핑' 이라 분류로는 갈라지지 않는다
        assertThat(TourismApiServiceImpl.isRecommendable(spot("롯데백화점/대전점", "쇼핑"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("모다아울렛/충주점", "쇼핑"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("충주시농수산물도매시장", "쇼핑"))).isFalse();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("대전원협노은농산물공판장", "쇼핑"))).isFalse();
    }

    @Test
    void 진짜_명소는_남긴다() {
        assertThat(TourismApiServiceImpl.isRecommendable(spot("국립공주박물관", "문화관광"))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("청남대", "기타관광"))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("활옥동굴", "자연관광"))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("청풍호반케이블카", "기타관광"))).isTrue();
    }

    @Test
    void 재래시장은_남긴다() {
        // 쇼핑 55 건 중 27 건이 재래시장이다. 쇼핑 전체를 버리면 이것들도 사라진다
        assertThat(TourismApiServiceImpl.isRecommendable(spot("공주산성시장", "쇼핑"))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("대전중앙시장", "쇼핑"))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot("병천시장", "쇼핑"))).isTrue();
    }

    @Test
    void 분류나_이름이_없어도_터지지_않는다() {
        assertThat(TourismApiServiceImpl.isRecommendable(spot("이름만있음", null))).isTrue();
        assertThat(TourismApiServiceImpl.isRecommendable(spot(null, "문화관광"))).isTrue();
    }
}
