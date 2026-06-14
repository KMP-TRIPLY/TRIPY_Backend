package com.kmp.Triply.domain.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "coupons",
    indexes = @Index(name = "idx_coupons_region_active", columnList = "partner_region, is_active")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "partner_name", nullable = false, length = 100)
    private String partnerName;

    @Column(name = "partner_region", nullable = false, length = 50)
    private String partnerRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private int discountValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", nullable = false, length = 10)
    private BarcodeType barcodeType = BarcodeType.QR;

    @Column(name = "min_rank", columnDefinition = "smallint")
    private Short minRank;

    @Column(name = "max_issue_count")
    private Integer maxIssueCount;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Coupon(String title, String partnerName, String partnerRegion, DiscountType discountType,
                   int discountValue, BarcodeType barcodeType, Short minRank, Integer maxIssueCount,
                   LocalDateTime validFrom, LocalDateTime validUntil) {
        this.title = title;
        this.partnerName = partnerName;
        this.partnerRegion = partnerRegion;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.barcodeType = barcodeType;
        this.minRank = minRank;
        this.maxIssueCount = maxIssueCount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public void deactivate() {
        this.isActive = false;
    }
}