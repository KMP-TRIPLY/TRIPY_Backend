package com.kmp.Triply.domain.trip.repository;

import com.kmp.Triply.domain.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByUserIdOrderByStartDateDesc(Long userId);
}