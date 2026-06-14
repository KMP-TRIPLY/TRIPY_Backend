package com.kmp.Triply.domain.place.service;

import com.kmp.Triply.domain.place.dto.request.PlaceCreateRequest;
import com.kmp.Triply.domain.place.dto.response.PlaceResponse;

import java.util.List;

public interface PlaceService {

    PlaceResponse addPlace(Long tripId, PlaceCreateRequest request);

    List<PlaceResponse> getPlaces(Long tripId);

    PlaceResponse updatePlace(Long placeId, PlaceCreateRequest request);

    void deletePlace(Long placeId);
}