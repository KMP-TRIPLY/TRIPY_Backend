package com.kmp.Triply.domain.trip.controller;

import com.kmp.Triply.domain.trip.dto.request.TripCreateRequest;
import com.kmp.Triply.domain.trip.dto.response.TripResponse;
import com.kmp.Triply.domain.trip.service.TripService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trip", description = "여행 일정 API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @Operation(summary = "여행 일정 생성", description = "사용자의 여행 일정과 예약 정보를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Parameter(description = "사용자 ID", example = "1") @RequestParam Long userId,
            @Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tripService.createTrip(userId, request)));
    }

    @Operation(summary = "내 여행 일정 목록 조회", description = "사용자 ID 기준으로 저장된 여행 일정 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponse>>> getMyTrips(
            @Parameter(description = "사용자 ID", example = "1") @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.getMyTrips(userId)));
    }

    @Operation(summary = "여행 일정 상세 조회", description = "여행 일정 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> getTrip(
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.getTrip(tripId)));
    }

    @Operation(summary = "여행 일정 수정", description = "사용자 본인의 여행 일정과 예약 정보를 수정합니다.")
    @PutMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @Parameter(description = "사용자 ID", example = "1") @RequestParam Long userId,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId,
            @Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tripService.updateTrip(userId, tripId, request)));
    }

    @Operation(summary = "여행 일정 삭제", description = "사용자 본인의 여행 일정을 삭제합니다.")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @Parameter(description = "사용자 ID", example = "1") @RequestParam Long userId,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId) {
        tripService.deleteTrip(userId, tripId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
