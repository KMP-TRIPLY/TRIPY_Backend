package com.kmp.Triply.domain.ranking.entity;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "leaderboards",
    indexes = {
        @Index(
            name = "idx_leaderboards_scope_period_region_user",
            columnList = "scope, period_type, region_code, user_id",
            unique = true
        ),
        @Index(
            name = "idx_leaderboards_scope_period_region_rank",
            columnList = "scope, period_type, region_code, rank"
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaderboardScope scope = LeaderboardScope.REGION;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private PeriodType periodType = PeriodType.WEEKLY;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int rank;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Leaderboard(LeaderboardScope scope, PeriodType periodType, String regionCode,
                        Course course, User user, int score, int rank) {
        this.scope = scope;
        this.periodType = periodType;
        this.regionCode = regionCode;
        this.course = course;
        this.user = user;
        this.score = score;
        this.rank = rank;
    }

    public void updateScoreAndRank(int score, int rank) {
        this.score = score;
        this.rank = rank;
    }
}