package com.kmp.Triply.domain.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 판정 응답을 읽어내지 못했을 때 통과시키지 않는지 본다 — 여기서 봐주면 아무 사진이나 다 통과한다.
 */
class PhotoVerificationServiceTest {

    private static final double THRESHOLD = 0.7;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 확신이_임계값_이상인_통과만_통과시킨다() throws Exception {
        assertThat(parse("""
                {"observed":"공산성 성벽과 금강","passed":true,"confidence":0.9,"reason":"성벽이 보인다"}
                """).passed()).isTrue();

        // 모델이 통과라고 해도 확신이 낮으면 통과가 아니다
        assertThat(parse("""
                {"observed":"흐릿한 담벼락","passed":true,"confidence":0.4,"reason":"단서가 약하다"}
                """).passed()).isFalse();
    }

    @Test
    void 거부_판정은_그대로_거부한다() throws Exception {
        PhotoVerdict verdict = parse("""
                {"observed":"휴대폰 화면 스크린샷","passed":false,"confidence":0.95,"reason":"스크린샷이다"}
                """);
        assertThat(verdict.passed()).isFalse();
        assertThat(verdict.note()).contains("스크린샷");
    }

    @Test
    void 응답이_망가지면_통과가_아니라_예외다() {
        // JSON 이 아예 없음
        assertThatThrownBy(() -> parse("판정할 수 없습니다")).isInstanceOf(IllegalStateException.class);
        // 빈 본문 (토큰 한도에서 잘린 경우)
        assertThatThrownBy(() -> parse("")).isInstanceOf(IllegalStateException.class);
        // 필드가 빠짐 — 기본값으로 메꿔 통과시키면 안 된다
        assertThatThrownBy(() -> parse("{\"reason\":\"좋아 보인다\"}")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void note_는_컬럼_길이를_넘지_않는다() {
        PhotoVerdict verdict = new PhotoVerdict(false, 0.1, "가".repeat(600), "나".repeat(600));
        assertThat(verdict.note().length()).isLessThanOrEqualTo(500);
    }

    private PhotoVerdict parse(String raw) throws Exception {
        return PhotoVerificationService.parse(objectMapper, raw, THRESHOLD);
    }
}
