package com.kmp.Triply.domain.main.service;

import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대시보드 추천 명소는 인기 순위대로 세 개를 고른다.
 * hubRank 는 시군구 안에서의 순위라 1 위가 시군구마다 있으므로, 시군구가 겹치면 안 된다.
 */
class MainServicePickPreviewsTest {

    private static RecommendationResponse spot(String title, String signguCd, int rank) {
        return RecommendationResponse.builder()
                .contentId(title)
                .title(title)
                .signguCd(signguCd)
                .signguNm(signguCd)
                .rank(rank)
                .build();
    }

    @Test
    void 인기_순위가_높은_순으로_고른다() {
        List<RecommendationResponse> picked = MainServiceImpl.pickTopSpots(List.of(
                spot("5위", "A", 5),
                spot("1위", "B", 1),
                spot("3위", "C", 3),
                spot("2위", "D", 2)));

        assertThat(picked).extracting(RecommendationResponse::getTitle)
                .containsExactly("1위", "2위", "3위");
    }

    @Test
    void 같은_시군구는_한_곳만_고른다() {
        List<RecommendationResponse> picked = MainServiceImpl.pickTopSpots(List.of(
                spot("청주1", "43111", 1),
                spot("청주2", "43111", 2),
                spot("청주3", "43111", 3),
                spot("충주1", "43130", 1),
                spot("제천1", "43150", 1)));

        assertThat(picked).extracting(RecommendationResponse::getTitle)
                .containsExactly("청주1", "충주1", "제천1");
    }

    @Test
    void 순위가_없는_곳은_뒤로_밀린다() {
        // hubRank 가 없으면 0 으로 파싱된다. 0 을 최상위로 보면 안 된다.
        List<RecommendationResponse> picked = MainServiceImpl.pickTopSpots(List.of(
                spot("순위없음", "A", 0),
                spot("10위", "B", 10),
                spot("20위", "C", 20)));

        assertThat(picked).extracting(RecommendationResponse::getTitle)
                .containsExactly("10위", "20위", "순위없음");
    }

    @Test
    void 세_개보다_적으면_있는_만큼만_준다() {
        assertThat(MainServiceImpl.pickTopSpots(List.of(spot("하나", "A", 1)))).hasSize(1);
        assertThat(MainServiceImpl.pickTopSpots(List.of())).isEmpty();
    }

    @Test
    void 원본_목록을_바꾸지_않는다() {
        List<RecommendationResponse> spots = new java.util.ArrayList<>(List.of(
                spot("5위", "A", 5),
                spot("1위", "B", 1)));

        MainServiceImpl.pickTopSpots(spots);

        assertThat(spots).extracting(RecommendationResponse::getTitle).containsExactly("5위", "1위");
    }
}
