package com.kmp.Triply.domain.game.controller;

import com.kmp.Triply.domain.game.dto.request.HintRequest;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.request.SpotArriveRequest;
import com.kmp.Triply.domain.game.dto.response.HintResponse;
import com.kmp.Triply.domain.game.dto.response.MissionSubmitResponse;
import com.kmp.Triply.domain.game.dto.response.PlayMissionResponse;
import com.kmp.Triply.domain.game.dto.response.SpotArriveResponse;
import com.kmp.Triply.domain.game.dto.response.TeamProgressResponse;
import com.kmp.Triply.domain.game.service.GamePlayService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Game Play", description = "미션 대결 진행 API (도착·미션 조회·힌트·제출)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GamePlayController {

    private final GamePlayService gamePlayService;

    @Operation(summary = "팀 진행 현황", description = "팀의 스팟별 진행 상태와 완료 미션 수, 총점을 조회합니다.")
    @GetMapping("/game-rooms/{roomId}/teams/{teamId}/progress")
    public ResponseEntity<ApiResponse<TeamProgressResponse>> getTeamProgress(
            Authentication authentication,
            @PathVariable Long roomId,
            @PathVariable Long teamId) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gamePlayService.getTeamProgress(userId, roomId, teamId)));
    }

    @Operation(summary = "스팟 도착 인증", description = "GPS 위치로 스팟 도착을 인증하면 해당 스팟의 미션이 활성화됩니다. (skipGps=true 시 위치 검증 생략)")
    @PostMapping("/game-rooms/{roomId}/spots/{spotId}/arrive")
    public ResponseEntity<ApiResponse<SpotArriveResponse>> arriveSpot(
            Authentication authentication,
            @PathVariable Long roomId,
            @PathVariable Long spotId,
            @Valid @RequestBody SpotArriveRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gamePlayService.arriveSpot(userId, roomId, spotId, request)));
    }

    @Operation(summary = "스팟 미션 조회", description = "도착한 스팟의 미션 목록을 조회합니다. 정답은 포함되지 않습니다.")
    @GetMapping("/game-rooms/{roomId}/spots/{spotId}/missions")
    public ResponseEntity<ApiResponse<List<PlayMissionResponse>>> getSpotMissions(
            Authentication authentication,
            @PathVariable Long roomId,
            @PathVariable Long spotId,
            @RequestParam Long teamId) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gamePlayService.getSpotMissions(userId, roomId, spotId, teamId)));
    }

    @Operation(summary = "힌트 요청", description = "미션 힌트를 열람합니다. 최초 열람 시 감점이 예약됩니다.")
    @PostMapping("/missions/{missionId}/hint")
    public ResponseEntity<ApiResponse<HintResponse>> requestHint(
            Authentication authentication,
            @PathVariable Long missionId,
            @Valid @RequestBody HintRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gamePlayService.requestHint(userId, missionId, request)));
    }

    @Operation(summary = "미션 제출", description = "미션 답을 제출하면 서버가 채점하고 팀 점수에 반영합니다.")
    @PostMapping("/missions/{missionId}/submit")
    public ResponseEntity<ApiResponse<MissionSubmitResponse>> submitMission(
            Authentication authentication,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionSubmitRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gamePlayService.submitMission(userId, missionId, request)));
    }
}