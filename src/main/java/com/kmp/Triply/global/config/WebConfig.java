package com.kmp.Triply.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebConfig {

    /** CORS 와 WebSocket 핸드셰이크가 같은 목록을 쓴다. */
    public static final List<String> ALLOWED_ORIGINS = List.of(
            "https://triply-six.vercel.app",
            "https://triply-six-*.vercel.app", // 우리 프로젝트 프리뷰 배포만
            "http://localhost:*"               // 로컬 개발 환경
    );

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public ObjectMapper objectMapper() {
        // 클래스패스의 모듈(jsr310 등) 등록. 안 하면 LocalDateTime 이 배열로 직렬화된다.
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
