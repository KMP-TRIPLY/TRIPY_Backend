package com.kmp.Triply.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "missions",
    indexes = @Index(name = "idx_missions_course_spot_id", columnList = "course_spot_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_spot_id", nullable = false)
    private CourseSpot courseSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false, length = 20)
    private MissionType missionType;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(columnDefinition = "text")
    private String answer;

    // [{label, value, is_correct}] 형태의 JSON 문자열
    @Column(columnDefinition = "json")
    private String choices;

    @Column(columnDefinition = "text")
    private String hint;

    @Column(name = "hint_penalty", nullable = false)
    private int hintPenalty = 150;

    @Column(name = "base_score", nullable = false)
    private int baseScore = 300;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Mission(CourseSpot courseSpot, MissionType missionType, String question,
                    String answer, String choices, String hint, int hintPenalty, int baseScore) {
        this.courseSpot = courseSpot;
        this.missionType = missionType;
        this.question = question;
        this.answer = answer;
        this.choices = choices;
        this.hint = hint;
        this.hintPenalty = hintPenalty;
        this.baseScore = baseScore;
    }
}