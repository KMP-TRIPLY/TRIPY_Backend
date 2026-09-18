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
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TripRepository tripRepository;

    @Transactional
    public PlaceResponse addPlace(Long userId, Long tripId, PlaceCreateRequest request) {
        Trip trip = tripRepository.findOwned(userId, tripId);

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

    public List<PlaceResponse> getPlaces(Long userId, Long tripId) {
        tripRepository.findOwned(userId, tripId);
        return placeRepository.findAllByTripIdOrderByVisitDate(tripId)
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional
    public PlaceResponse updatePlace(Long userId, Long tripId, Long placeId, PlaceCreateRequest request) {
        Place place = findOwnedPlace(userId, tripId, placeId);
        place.update(request.getName(), request.getAddress(), request.getVisitDate(), request.getMemo());
        return PlaceResponse.from(place);
    }

    @Transactional
    public void deletePlace(Long userId, Long tripId, Long placeId) {
        placeRepository.delete(findOwnedPlace(userId, tripId, placeId));
    }

    /** placeId 만 믿으면 경로의 tripId 와 무관한 남의 장소를 고칠 수 있다. 둘 다 확인한다. */
    private Place findOwnedPlace(Long userId, Long tripId, Long placeId) {
        tripRepository.findOwned(userId, tripId);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.getTrip().getId().equals(tripId)) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        return place;
    }
}
