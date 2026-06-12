package com.kmp.Triply.domain.course.entity;

import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "courses",
    indexes = @Index(name = "idx_courses_region_active", columnList = "region_code, is_active")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;

    @Column(nullable = false, length = 50)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty = Difficulty.NORMAL;

    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes = 120;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 20)
    private CourseType courseType = CourseType.GENERAL;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Course(String title, String description, String regionCode, String city,
                   Difficulty difficulty, int estimatedMinutes, CourseType courseType,
                   boolean isAiGenerated, User createdBy) {
        this.title = title;
        this.description = description;
        this.regionCode = regionCode;
        this.city = city;
        this.difficulty = difficulty;
        this.estimatedMinutes = estimatedMinutes;
        this.courseType = courseType;
        this.isAiGenerated = isAiGenerated;
        this.createdBy = createdBy;
    }

    public void deactivate() {
        this.isActive = false;
    }
}