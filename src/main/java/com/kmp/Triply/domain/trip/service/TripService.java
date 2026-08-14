package com.kmp.Triply.domain.trip.service;

import com.kmp.Triply.domain.trip.dto.request.TripCreateRequest;
import com.kmp.Triply.domain.trip.dto.response.TripResponse;

import java.util.List;

public interface TripService {

    TripResponse createTrip(Long userId, TripCreateRequest request);

    List<TripResponse> getMyTrips(Long userId);

    TripResponse getTrip(Long userId, Long tripId);

    TripResponse updateTrip(Long userId, Long tripId, TripCreateRequest request);

    void deleteTrip(Long userId, Long tripId);
}