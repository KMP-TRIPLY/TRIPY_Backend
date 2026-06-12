package com.kmp.Triply.domain.course.entity;

import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "course_spots",
    indexes = {
        @Index(name = "idx_course_spots_course_seq", columnList = "course_id, sequence_order", unique = true),
        @Index(name = "idx_course_spots_course_id", columnList = "course_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourism_spot_id", nullable = false)
    private TourismSpot tourismSpot;

    @Column(name = "sequence_order", nullable = false, columnDefinition = "smallint")
    private short sequenceOrder;

    @Column(name = "story_text", columnDefinition = "text")
    private String storyText;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "radius_meters", nullable = false)
    private int radiusMeters = 200;

    @Builder
    private CourseSpot(Course course, TourismSpot tourismSpot, short sequenceOrder,
                       String storyText, BigDecimal lat, BigDecimal lng, int radiusMeters) {
        this.course = course;
        this.tourismSpot = tourismSpot;
        this.sequenceOrder = sequenceOrder;
        this.storyText = storyText;
        this.lat = lat;
        this.lng = lng;
        this.radiusMeters = radiusMeters;
    }
}