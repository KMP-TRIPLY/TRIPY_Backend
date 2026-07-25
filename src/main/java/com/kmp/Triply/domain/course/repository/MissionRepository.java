package com.kmp.Triply.domain.course.repository;

import com.kmp.Triply.domain.course.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByCourseSpotIdOrderByIdAsc(Long courseSpotId);

    List<Mission> findAllByCourseSpotIdInOrderByIdAsc(List<Long> courseSpotIds);
}
