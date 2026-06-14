package com.kmp.Triply.domain.place.dto.response;

import com.kmp.Triply.domain.place.entity.Place;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PlaceResponse {

    private Long id;
    private Long tripId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDate visitDate;
    private String memo;

    public static PlaceResponse from(Place place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .tripId(place.getTrip().getId())
                .name(place.getName())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .visitDate(place.getVisitDate())
                .memo(place.getMemo())
                .build();
    }
}