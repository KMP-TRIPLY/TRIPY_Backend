package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.course.entity.CourseSpot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "game_progress",
    indexes = {
        @Index(name = "idx_game_progress_team_spot", columnList = "team_id, course_spot_id", unique = true),
        @Index(name = "idx_game_progress_team_id", columnList = "team_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_spot_id", nullable = false)
    private CourseSpot courseSpot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ProgressStatus status = ProgressStatus.LOCKED;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private GameProgress(Team team, CourseSpot courseSpot, ProgressStatus status) {
        this.team = team;
        this.courseSpot = courseSpot;
        this.status = status;
    }

    public void activate() {
        this.status = ProgressStatus.ACTIVE;
        this.arrivedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}