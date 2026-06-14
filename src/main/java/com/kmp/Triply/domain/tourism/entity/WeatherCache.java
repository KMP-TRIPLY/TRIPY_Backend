package com.kmp.Triply.domain.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_code", nullable = false, unique = true, length = 10)
    private String regionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WeatherCondition condition;

    @Column(name = "temp_celsius", columnDefinition = "smallint")
    private Short tempCelsius;

    @Column(columnDefinition = "smallint")
    private Short humidity;

    @Column(name = "wind_speed", length = 10)
    private String windSpeed;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private WeatherCache(String regionCode, WeatherCondition condition, Short tempCelsius,
                         Short humidity, String windSpeed, LocalDateTime expiresAt) {
        this.regionCode = regionCode;
        this.condition = condition;
        this.tempCelsius = tempCelsius;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.fetchedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void refresh(WeatherCondition condition, Short tempCelsius, Short humidity,
                        String windSpeed, LocalDateTime expiresAt) {
        this.condition = condition;
        this.tempCelsius = tempCelsius;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.fetchedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
}