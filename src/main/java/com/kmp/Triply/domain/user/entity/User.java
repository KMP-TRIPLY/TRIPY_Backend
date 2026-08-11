package com.kmp.Triply.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 유니크 제약은 "탈퇴하지 않은 계정끼리만" 이어야 해서 부분 인덱스로 관리한다.
 * JPA 로는 표현이 안 되므로 아래 DDL 을 DB 에 직접 적용한다.
 * <pre>
 * CREATE UNIQUE INDEX idx_users_email  ON users (email)                      WHERE deleted_at IS NULL;
 * CREATE UNIQUE INDEX idx_users_social ON users (social_provider, social_id) WHERE deleted_at IS NULL;
 * </pre>
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_img", length = 500)
    private String profileImg;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false, length = 255)
    private String socialId;

    @Column(nullable = false)
    private int level = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private User(String email, String nickname, String profileImg,
                 SocialProvider socialProvider, String socialId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
    }

    /** 전달된 값만 갱신 (null 이면 기존 값 유지 — 닉네임만 수정해도 프로필 이미지가 지워지지 않도록) */
    public void updateProfile(String nickname, String profileImg) {
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (profileImg != null) this.profileImg = profileImg;
    }

    public void levelUp() {
        this.level++;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}