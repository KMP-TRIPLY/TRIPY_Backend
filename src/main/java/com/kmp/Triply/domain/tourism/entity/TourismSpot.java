package com.kmp.Triply.domain.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tourism_spots",
    indexes = @Index(name = "idx_tourism_spots_lat_lng", columnList = "lat, lng")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourismSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_api_content_id", nullable = false, unique = true, length = 50)
    private String openApiContentId;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SpotCategory category;

    @Column(name = "category_large", length = 50)
    private String categoryLarge;   // TourAPI hubCtgryLclsNm

    @Column(name = "category_middle", length = 50)
    private String categoryMiddle;  // TourAPI hubCtgryMclsNm

    @Column(length = 300)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "area_code", length = 10)
    private String areaCode;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    @Builder
    private TourismSpot(String openApiContentId, String name, SpotCategory category,
                        String categoryLarge, String categoryMiddle,
                        String address, BigDecimal lat, BigDecimal lng, String thumbnailUrl,
                        String areaCode, Integer rank) {
        this.openApiContentId = openApiContentId;
        this.name = name;
        this.category = category;
        this.categoryLarge = categoryLarge;
        this.categoryMiddle = categoryMiddle;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.thumbnailUrl = thumbnailUrl;
        this.areaCode = areaCode;
        this.rank = rank;
        this.cachedAt = LocalDateTime.now();
    }

    public void update(String name, SpotCategory category,
                       String categoryLarge, String categoryMiddle, String address,
                       BigDecimal lat, BigDecimal lng, String thumbnailUrl,
                       String areaCode, Integer rank) {
        this.name = name;
        this.category = category;
        this.categoryLarge = categoryLarge;
        this.categoryMiddle = categoryMiddle;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.thumbnailUrl = thumbnailUrl;
        this.areaCode = areaCode;
        this.rank = rank;
        this.cachedAt = LocalDateTime.now();
    }
}