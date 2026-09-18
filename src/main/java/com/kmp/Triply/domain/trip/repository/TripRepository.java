package com.kmp.Triply.domain.trip.repository;

import com.kmp.Triply.domain.trip.entity.Trip;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByUserIdOrderByStartDateDesc(Long userId);

    long countByUserId(Long userId);

    Optional<Trip> findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateAsc(Long userId, LocalDate today);

    /** 조회·수정·삭제가 모두 거쳐야 하는 소유권 검사. 한 군데로 모아야 빠뜨리지 않는다. 없는 여행이면 404, 남의 여행이면 403. */
    default Trip findOwned(Long userId, Long tripId) {
        Trip trip = findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));
        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }
        return trip;
    }
}
