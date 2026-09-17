package com.kmp.Triply.domain.course.entity;

import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

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

    /** 실내 진행 정도. 비 오는 날 추천에 쓴다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "indoor_type", nullable = false, length = 10)
    private IndoorType indoorType = IndoorType.MIXED;

    /** 상황·취향 태그. 없을 수도 있다. */
    @ElementCollection(targetClass = CourseTag.class, fetch = FetchType.LAZY)
    @CollectionTable(
        name = "course_tags",
        joinColumns = @JoinColumn(name = "course_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_course_tags_course_tag", columnNames = {"course_id", "tag"})
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 20)
    private Set<CourseTag> tags = EnumSet.noneOf(CourseTag.class);

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
                   IndoorType indoorType, Set<CourseTag> tags,
                   boolean isAiGenerated, User createdBy) {
        this.title = title;
        this.description = description;
        this.regionCode = regionCode;
        this.city = city;
        this.difficulty = difficulty;
        this.estimatedMinutes = estimatedMinutes;
        this.courseType = courseType;
        // 안 정해두면 필터에서 통째로 빠지므로, 모르면 "섞여 있다" 로 둔다.
        this.indoorType = indoorType == null ? IndoorType.MIXED : indoorType;
        this.tags = tags == null || tags.isEmpty()
                ? EnumSet.noneOf(CourseTag.class) : EnumSet.copyOf(tags);
        this.isAiGenerated = isAiGenerated;
        this.createdBy = createdBy;
    }

    public void deactivate() {
        this.isActive = false;
    }
}