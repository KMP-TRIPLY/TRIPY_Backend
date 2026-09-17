package com.kmp.Triply.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_social_id", columnList = "social_id", unique = true)
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_img", length = 500)
    private String profileImg;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false, unique = true, length = 255)
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

    /**
     * 탈퇴한 계정에서 소셜 연결과 이메일을 떼어낸다.
     *
     * <p>email 과 social_id 는 유니크라 자리를 비워야 같은 소셜 계정으로 다시 가입할 수 있다.
     * 행 자체는 지우지 않는다 — 게임 기록·랭킹·리워드가 user_id 를 참조하고 있어서
     * 지우면 남은 팀원들의 과거 기록까지 깨진다. 남는 건 익명 껍데기다.
     */
    public void releaseSocialIdentity() {
        this.socialId = "withdrawn_" + this.id;
        this.email = "withdrawn_" + this.id + "@triply.invalid";
        this.nickname = "탈퇴한 사용자";
        this.profileImg = null;
    }
}