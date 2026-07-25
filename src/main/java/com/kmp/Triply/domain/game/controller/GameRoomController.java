package com.kmp.Triply.domain.game.controller;

import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomJoinRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomStartRequest;
import com.kmp.Triply.domain.game.dto.response.GameRoomJoinResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomResponse;
import com.kmp.Triply.domain.game.dto.response.TeamMemberResponse;
import com.kmp.Triply.domain.game.dto.response.TeamRankingResponse;
import com.kmp.Triply.domain.game.service.GameRoomService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Game Room", description = "게임방 및 팀 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @Operation(summary = "게임방 생성", description = "코스를 선택해 새 게임방을 생성하고 생성자를 호스트 및 팀 리더로 참여시킵니다.")
    @PostMapping("/game-rooms")
    public ResponseEntity<ApiResponse<GameRoomJoinResponse>> createRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(gameRoomService.createRoom(userId, request)));
    }

    @Operation(summary = "게임방 참여", description = "방 코드를 사용해 대기 중인 게임방에 참여합니다. teamId가 있으면 기존 팀에, 없으면 새 팀으로 참여합니다.")
    @PostMapping("/game-rooms/join")
    public ResponseEntity<ApiResponse<GameRoomJoinResponse>> joinRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomJoinRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.joinRoom(userId, request)));
    }

    @Operation(summary = "게임 시작", description = "호스트가 대기 중인 게임방을 시작 상태로 변경하고 모든 팀을 플레이 상태로 전환합니다.")
    @PostMapping("/game-rooms/start")
    public ResponseEntity<ApiResponse<GameRoomResponse>> startRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomStartRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.startRoom(userId, request)));
    }

    @Operation(summary = "게임 종료", description = "호스트가 진행 중인 게임방을 종료하고 팀 순위 및 최종 랭킹을 확정합니다.")
    @PostMapping("/game-rooms/{id}/end")
    public ResponseEntity<ApiResponse<GameRoomResponse>> endRoom(
            Authentication authentication,
            @Parameter(description = "게임방 ID", example = "10") @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.endRoom(userId, id)));
    }

    @Operation(summary = "게임방 팀 랭킹 조회", description = "게임방 내 팀별 점수와 순위를 조회합니다.")
    @GetMapping("/game-rooms/{roomId}/rankings")
    public ResponseEntity<ApiResponse<List<TeamRankingResponse>>> getRankings(
            @Parameter(description = "게임방 ID", example = "10") @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.getRankings(roomId)));
    }

    @Operation(summary = "팀 멤버 조회", description = "팀에 속한 멤버의 프로필과 여행 성향 태그를 조회합니다.")
    @GetMapping("/teams/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
            @Parameter(description = "팀 ID", example = "4") @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.getTeamMembers(teamId)));
    }

    @Operation(summary = "게임방 멤버 강퇴", description = "호스트가 대기 중인 게임방에서 특정 멤버를 강퇴합니다.")
    @DeleteMapping("/game-rooms/{roomId}/members/{userId}/kick")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            Authentication authentication,
            @Parameter(description = "게임방 ID", example = "10") @PathVariable Long roomId,
            @Parameter(description = "강퇴할 사용자 ID", example = "3") @PathVariable Long userId) {
        Long hostUserId = (Long) authentication.getPrincipal();
        gameRoomService.kickMember(hostUserId, roomId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
