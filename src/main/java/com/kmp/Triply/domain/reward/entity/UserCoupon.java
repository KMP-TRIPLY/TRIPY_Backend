package com.kmp.Triply.domain.reward.entity;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_coupons",
    indexes = {
        @Index(name = "idx_user_coupons_user_status", columnList = "user_id, status"),
        @Index(name = "idx_user_coupons_coupon_code", columnList = "coupon_code", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @Column(name = "coupon_code", nullable = false, unique = true, length = 30)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CouponStatus status = CouponStatus.ISSUED;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private UserCoupon(User user, Coupon coupon, GameRoom gameRoom,
                       String couponCode, LocalDateTime expiresAt) {
        this.user = user;
        this.coupon = coupon;
        this.gameRoom = gameRoom;
        this.couponCode = couponCode;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }
}