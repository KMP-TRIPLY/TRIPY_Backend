package com.kmp.Triply.domain.trip.controller;

import com.kmp.Triply.domain.trip.dto.request.TripCreateRequest;
import com.kmp.Triply.domain.trip.dto.response.TripResponse;
import com.kmp.Triply.domain.trip.service.TripService;
import com.kmp.Triply.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @RequestParam Long userId,
            @Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tripService.createTrip(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponse>>> getMyTrips(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.getMyTrips(userId)));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> getTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.getTrip(tripId)));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @RequestParam Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.updateTrip(userId, tripId, request)));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @RequestParam Long userId,
            @PathVariable Long tripId) {
        tripService.deleteTrip(userId, tripId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}