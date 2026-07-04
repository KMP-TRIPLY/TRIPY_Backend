package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "mission_attempts",
    indexes = {
        @Index(name = "idx_mission_attempts_progress_id", columnList = "game_progress_id"),
        @Index(name = "idx_mission_attempts_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_progress_id", nullable = false)
    private GameProgress gameProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false, length = 20)
    private AttemptType attemptType;

    @Column(name = "submitted_value", columnDefinition = "text")
    private String submittedValue;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private AttemptResult result;

    @Column(name = "score_earned", nullable = false)
    private int scoreEarned = 0;

    @Column(name = "hint_used", nullable = false)
    private boolean hintUsed = false;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Builder
    private MissionAttempt(GameProgress gameProgress, Mission mission, User user,
                           AttemptType attemptType, String submittedValue, String photoUrl,
                           AttemptResult result, int scoreEarned, boolean hintUsed) {
        this.gameProgress = gameProgress;
        this.mission = mission;
        this.user = user;
        this.attemptType = attemptType;
        this.submittedValue = submittedValue;
        this.photoUrl = photoUrl;
        this.result = result;
        this.scoreEarned = scoreEarned;
        this.hintUsed = hintUsed;
        this.attemptedAt = LocalDateTime.now();
    }

    public void judgeResult(AttemptResult result, int scoreEarned) {
        this.result = result;
        this.scoreEarned = scoreEarned;
    }
}