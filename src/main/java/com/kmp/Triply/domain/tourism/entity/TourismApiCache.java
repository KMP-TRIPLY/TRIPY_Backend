package com.kmp.Triply.domain.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tourism_api_cache",
    indexes = @Index(name = "idx_tourism_api_cache_region_api", columnList = "region_code, api_type")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourismApiCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false, length = 30)
    private ApiType apiType;

    @Column(name = "content_id", nullable = false, unique = true, length = 50)
    private String contentId;

    @Column(name = "raw_json", nullable = false, columnDefinition = "text")
    private String rawJson;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private TourismApiCache(ApiType apiType, String contentId, String rawJson,
                            String regionCode, LocalDateTime expiresAt) {
        this.apiType = apiType;
        this.contentId = contentId;
        this.rawJson = rawJson;
        this.regionCode = regionCode;
        this.fetchedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void refresh(String rawJson, LocalDateTime expiresAt) {
        this.rawJson = rawJson;
        this.fetchedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
}