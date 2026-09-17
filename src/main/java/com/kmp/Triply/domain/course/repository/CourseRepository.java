package com.kmp.Triply.domain.course.repository;

import com.kmp.Triply.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 활성 코스 + 태그. 태그는 별도 테이블이라 fetch join 으로 함께 읽는다 —
     * 안 그러면 목록 길이만큼 태그 조회 쿼리가 따라붙는다.
     */
    @Query("""
            select distinct course from Course course
            left join fetch course.tags
            where course.isActive = true
              and (:regionCode is null or course.regionCode = :regionCode)
              and (:city is null or course.city = :city)
            order by course.createdAt desc
            """)
    List<Course> findActiveCourses(@Param("regionCode") String regionCode, @Param("city") String city);

    @Query("""
            select distinct course.regionCode from Course course
            where course.isActive = true
            order by course.regionCode asc
            """)
    List<String> findActiveCourseRegionCodes();

    boolean existsByTitle(String title);
}
