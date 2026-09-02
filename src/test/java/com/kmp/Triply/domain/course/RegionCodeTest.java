package com.kmp.Triply.domain.course;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 클라이언트가 시도 코드를 모르므로 도시 이름으로 찾는다.
 * 틀린 코드를 넣는 것보다 못 찾는 편이 안전하다 - 애매하면 빈 값이어야 한다.
 */
class RegionCodeTest {

    @Test
    void 시군구_이름으로_시도_코드를_찾는다() {
        assertThat(RegionCode.resolve("공주")).contains("44");
        assertThat(RegionCode.resolve("강릉")).contains("51");
        assertThat(RegionCode.resolve("여수")).contains("46");
    }

    @Test
    void 접미사가_붙어도_찾는다() {
        assertThat(RegionCode.resolve("공주시")).contains("44");
        assertThat(RegionCode.resolve("부여군")).contains("44");
        assertThat(RegionCode.resolve("충청남도 공주시")).contains("44");
    }

    @Test
    void 한_시도의_여러_도시가_섞여_있으면_그_시도로_찾는다() {
        assertThat(RegionCode.resolve("공주·부여")).contains("44");
    }

    @Test
    void 서로_다른_시도가_섞여_있으면_정하지_않는다() {
        // "경기도 광주시" 는 광주광역시(29) 와 경기도(41) 키워드를 모두 포함한다.
        assertThat(RegionCode.resolve("경기도 광주시")).isEmpty();
        assertThat(RegionCode.resolve("공주 강릉")).isEmpty();
    }

    @Test
    void 두_시도에_같은_이름이_있는_시군구는_쓰지_않는다() {
        // 고성군은 강원과 경남에 모두 있다.
        assertThat(RegionCode.resolve("고성")).isEmpty();
    }

    @Test
    void 모르는_도시나_빈_값은_찾지_못한다() {
        assertThat(RegionCode.resolve("gongju")).isEmpty();
        assertThat(RegionCode.resolve("도쿄")).isEmpty();
        assertThat(RegionCode.resolve("")).isEmpty();
        assertThat(RegionCode.resolve(null)).isEmpty();
    }

    @Test
    void 시도_이름은_코드로_찾는다() {
        assertThat(RegionCode.nameOf("44")).isEqualTo("충청남도");
        assertThat(RegionCode.nameOf("99")).isEqualTo("99");
        assertThat(RegionCode.exists("44")).isTrue();
        assertThat(RegionCode.exists("99")).isFalse();
    }
}
