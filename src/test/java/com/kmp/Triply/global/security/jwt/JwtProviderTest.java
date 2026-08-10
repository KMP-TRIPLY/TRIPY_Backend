package com.kmp.Triply.global.security.jwt;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 블랙리스트 TTL 계산과 Bearer 파싱만 검증한다.
 */
class JwtProviderTest {

    private static final long ACCESS_EXPIRY = 60_000L;
    private static final String SECRET =
            Base64.getEncoder().encodeToString("triply-test-secret-key-32bytes!!".getBytes());

    private final JwtProvider provider = new JwtProvider(SECRET, ACCESS_EXPIRY, 600_000L);

    @Test
    void 블랙리스트_TTL은_토큰_유효기간을_넘지_않는다() {
        long remaining = provider.getRemainingMillis(provider.generateAccessToken(1L));

        // exp 는 초 단위로 잘려서 최대 1초까지 짧아질 수 있다
        assertThat(remaining).isGreaterThan(ACCESS_EXPIRY - 2_000).isLessThanOrEqualTo(ACCESS_EXPIRY);
    }

    @Test
    void Bearer_형식이_아니면_null() {
        assertThat(JwtProvider.resolveBearer("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
        assertThat(JwtProvider.resolveBearer("abc.def.ghi")).isNull();
        assertThat(JwtProvider.resolveBearer("")).isNull();
        assertThat(JwtProvider.resolveBearer(null)).isNull();
    }
}
