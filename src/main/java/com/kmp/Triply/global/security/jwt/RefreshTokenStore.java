package com.kmp.Triply.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 리프레시 토큰을 Redis 에 사용자당 1개씩 보관한다.
 * 리프레시 토큰 자체가 userId 를 담은 JWT 라서 역방향 조회 인덱스가 필요 없다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String PREFIX = "rt:";

    private final StringRedisTemplate redis;

    /** 새로 저장하면 이전 토큰은 덮여서 무효가 된다(= 재로그인 시 기존 기기 로그아웃). */
    public void save(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(PREFIX + userId, refreshToken, ttl);
    }

    public boolean matches(Long userId, String refreshToken) {
        return refreshToken.equals(redis.opsForValue().get(PREFIX + userId));
    }

    public void delete(Long userId) {
        redis.delete(PREFIX + userId);
    }
}
