package com.kmp.Triply.domain.course.repository;

import com.kmp.Triply.domain.course.entity.CourseSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSpotRepository extends JpaRepository<CourseSpot, Long> {

    List<CourseSpot> findAllByCourseIdOrderBySequenceOrderAsc(Long courseId);
}
