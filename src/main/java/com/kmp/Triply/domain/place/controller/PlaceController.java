package com.kmp.Triply.domain.place.controller;

import com.kmp.Triply.domain.place.dto.request.PlaceCreateRequest;
import com.kmp.Triply.domain.place.dto.response.PlaceResponse;
import com.kmp.Triply.domain.place.service.PlaceService;
import com.kmp.Triply.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PlaceResponse>> addPlace(
            @PathVariable Long tripId,
            @Valid @RequestBody PlaceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(placeService.addPlace(tripId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getPlaces(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.getPlaces(tripId)));
    }

    @PutMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> updatePlace(
            @PathVariable Long tripId,
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.updatePlace(placeId, request)));
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(
            @PathVariable Long tripId,
            @PathVariable Long placeId) {
        placeService.deletePlace(placeId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}