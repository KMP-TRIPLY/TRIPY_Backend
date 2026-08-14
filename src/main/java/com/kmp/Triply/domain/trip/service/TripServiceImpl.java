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
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
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

    @Override
    public List<TripResponse> getMyTrips(Long userId) {
        return tripRepository.findAllByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    @Override
    public TripResponse getTrip(Long userId, Long tripId) {
        return TripResponse.from(findOwnedTrip(userId, tripId));
    }

    @Override
    @Transactional
    public TripResponse updateTrip(Long userId, Long tripId, TripCreateRequest request) {
        Trip trip = findOwnedTrip(userId, tripId);
        trip.update(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
        return TripResponse.from(trip);
    }

    @Override
    @Transactional
    public void deleteTrip(Long userId, Long tripId) {
        tripRepository.delete(findOwnedTrip(userId, tripId));
    }

    /** 조회·수정·삭제가 모두 거쳐야 하는 소유권 검사. 한 군데로 모아야 빠뜨리지 않는다. */
    private Trip findOwnedTrip(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }
        return trip;
    }
}