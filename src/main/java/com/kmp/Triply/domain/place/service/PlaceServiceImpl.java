package com.kmp.Triply.domain.place.service;

import com.kmp.Triply.domain.place.dto.request.PlaceCreateRequest;
import com.kmp.Triply.domain.place.dto.response.PlaceResponse;
import com.kmp.Triply.domain.place.entity.Place;
import com.kmp.Triply.domain.place.repository.PlaceRepository;
import com.kmp.Triply.domain.trip.entity.Trip;
import com.kmp.Triply.domain.trip.repository.TripRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional
    public PlaceResponse addPlace(Long tripId, PlaceCreateRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        Place place = Place.builder()
                .trip(trip)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .visitDate(request.getVisitDate())
                .memo(request.getMemo())
                .build();

        return PlaceResponse.from(placeRepository.save(place));
    }

    @Override
    public List<PlaceResponse> getPlaces(Long tripId) {
        return placeRepository.findAllByTripIdOrderByVisitDate(tripId)
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public PlaceResponse updatePlace(Long placeId, PlaceCreateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        place.update(request.getName(), request.getAddress(), request.getVisitDate(), request.getMemo());
        return PlaceResponse.from(place);
    }

    @Override
    @Transactional
    public void deletePlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        placeRepository.delete(place);
    }
}