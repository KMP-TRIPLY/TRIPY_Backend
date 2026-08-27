package com.kmp.Triply.domain.place.controller;

import com.kmp.Triply.domain.place.dto.request.PlaceCreateRequest;
import com.kmp.Triply.domain.place.dto.response.PlaceResponse;
import com.kmp.Triply.domain.place.service.PlaceService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Place", description = "여행 장소 API")
@RestController
@RequestMapping("/api/trips/{tripId}/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "여행 장소 추가", description = "본인의 여행 일정에 방문 장소를 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PlaceResponse>> addPlace(
            Authentication authentication,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId,
            @Valid @RequestBody PlaceCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(placeService.addPlace(userId, tripId, request)));
    }

    @Operation(summary = "여행 장소 목록 조회", description = "본인의 여행 일정에 저장된 방문 장소 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getPlaces(
            Authentication authentication,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(placeService.getPlaces(userId, tripId)));
    }

    @Operation(summary = "여행 장소 수정", description = "본인의 여행 일정에 있는 방문 장소 정보를 수정합니다.")
    @PutMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> updatePlace(
            Authentication authentication,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId,
            @Parameter(description = "방문 장소 ID", example = "10") @PathVariable Long placeId,
            @Valid @RequestBody PlaceCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(placeService.updatePlace(userId, tripId, placeId, request)));
    }

    @Operation(summary = "여행 장소 삭제", description = "본인의 여행 일정에서 방문 장소를 삭제합니다.")
    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(
            Authentication authentication,
            @Parameter(description = "여행 일정 ID", example = "1") @PathVariable Long tripId,
            @Parameter(description = "방문 장소 ID", example = "10") @PathVariable Long placeId) {
        Long userId = (Long) authentication.getPrincipal();
        placeService.deletePlace(userId, tripId, placeId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
