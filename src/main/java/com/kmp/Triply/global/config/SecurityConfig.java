package com.kmp.Triply.global.config;

import com.kmp.Triply.global.security.jwt.JwtAuthenticationFilter;
import com.kmp.Triply.global.security.jwt.JwtProvider;
import com.kmp.Triply.global.security.jwt.TokenBlacklist;
import com.kmp.Triply.global.security.oauth2.CustomOAuth2UserService;
import com.kmp.Triply.global.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.kmp.Triply.global.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.kmp.Triply.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final TokenBlacklist tokenBlacklist;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // logout 은 인증 필요 (permitAll 이면 authentication 이 null 이라 500 난다)
                    "/api/auth/refresh",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/ws/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestRepository(authorizationRequestRepository))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(successHandler)
                .failureHandler(failureHandler))
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, tokenBlacklist),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
