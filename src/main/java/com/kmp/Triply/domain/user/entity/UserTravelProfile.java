package com.kmp.Triply.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_travel_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTravelProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "style_type", nullable = false, length = 30)
    private StyleType styleType;

    @Column(name = "score_history", nullable = false, columnDefinition = "smallint default 0")
    private short scoreHistory = 0;

    @Column(name = "score_adventure", nullable = false, columnDefinition = "smallint default 0")
    private short scoreAdventure = 0;

    @Column(name = "score_food", nullable = false, columnDefinition = "smallint default 0")
    private short scoreFood = 0;

    @Column(name = "score_healing", nullable = false, columnDefinition = "smallint default 0")
    private short scoreHealing = 0;

    @Column(name = "score_culture", nullable = false, columnDefinition = "smallint default 0")
    private short scoreCulture = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_type", nullable = false, length = 20)
    private MoveType moveType = MoveType.WALK;

    @Enumerated(EnumType.STRING)
    @Column(name = "companion_type", nullable = false, length = 20)
    private CompanionType companionType = CompanionType.SOLO;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private UserTravelProfile(User user, StyleType styleType, MoveType moveType, CompanionType companionType) {
        this.user = user;
        this.styleType = styleType;
        this.moveType = moveType;
        this.companionType = companionType;
    }

    public void updateScores(short history, short adventure, short food, short healing, short culture) {
        this.scoreHistory = history;
        this.scoreAdventure = adventure;
        this.scoreFood = food;
        this.scoreHealing = healing;
        this.scoreCulture = culture;
    }

    public void updatePreferences(MoveType moveType, CompanionType companionType) {
        this.moveType = moveType;
        this.companionType = companionType;
    }

    public void updateStyleType(StyleType styleType) {
        this.styleType = styleType;
    }
}