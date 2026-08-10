package com.kmp.Triply.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 로그아웃한 액세스 토큰을 만료 시점까지만 Redis 에 담아둔다.
 * JWT 는 서버가 상태를 안 가져서, 이게 없으면 로그아웃해도 남은 유효기간 동안 그대로 통과한다.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklist {

    private static final String PREFIX = "bl:";

    private final StringRedisTemplate redis;
    private final JwtProvider jwtProvider;

    public void add(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        long ttl = jwtProvider.getRemainingMillis(accessToken);
        if (ttl > 0) {
            // 만료 후엔 JWT 검증에서 어차피 걸리니 TTL 지나면 알아서 사라지게 둔다
            redis.opsForValue().set(PREFIX + accessToken, "", Duration.ofMillis(ttl));
        }
    }

    public boolean contains(String accessToken) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + accessToken));
    }
}
