package com.kmp.Triply.domain.place.repository;

import com.kmp.Triply.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findAllByTripIdOrderByVisitDate(Long tripId);
}