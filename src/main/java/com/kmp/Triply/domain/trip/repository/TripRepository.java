package com.kmp.Triply.domain.trip.repository;

import com.kmp.Triply.domain.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByUserIdOrderByStartDateDesc(Long userId);

    long countByUserId(Long userId);

    Optional<Trip> findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);
}