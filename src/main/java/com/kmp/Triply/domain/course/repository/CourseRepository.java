package com.kmp.Triply.domain.course.repository;

import com.kmp.Triply.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
