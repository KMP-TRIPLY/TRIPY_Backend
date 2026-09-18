package com.kmp.Triply.domain.trip.service;

import com.kmp.Triply.domain.trip.dto.request.TripCreateRequest;
import com.kmp.Triply.domain.trip.dto.response.TripResponse;
import com.kmp.Triply.domain.trip.entity.Trip;
import com.kmp.Triply.domain.trip.repository.TripRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public TripResponse createTrip(Long userId, TripCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Trip trip = Trip.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return TripResponse.from(tripRepository.save(trip));
    }

    public List<TripResponse> getMyTrips(Long userId) {
        return tripRepository.findAllByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    public TripResponse getTrip(Long userId, Long tripId) {
        return TripResponse.from(tripRepository.findOwned(userId, tripId));
    }

    @Transactional
    public TripResponse updateTrip(Long userId, Long tripId, TripCreateRequest request) {
        Trip trip = tripRepository.findOwned(userId, tripId);
        trip.update(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
        return TripResponse.from(trip);
    }

    @Transactional
    public void deleteTrip(Long userId, Long tripId) {
        tripRepository.delete(tripRepository.findOwned(userId, tripId));
    }

}