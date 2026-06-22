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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/game-rooms")
    public ResponseEntity<ApiResponse<GameRoomJoinResponse>> createRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(gameRoomService.createRoom(userId, request)));
    }

    @PostMapping("/game-rooms/join")
    public ResponseEntity<ApiResponse<GameRoomJoinResponse>> joinRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomJoinRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.joinRoom(userId, request)));
    }

    @PostMapping("/game-rooms/start")
    public ResponseEntity<ApiResponse<GameRoomResponse>> startRoom(
            Authentication authentication,
            @Valid @RequestBody GameRoomStartRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.startRoom(userId, request)));
    }

    @PostMapping("/game-rooms/{id}/end")
    public ResponseEntity<ApiResponse<GameRoomResponse>> endRoom(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.endRoom(userId, id)));
    }

    @GetMapping("/game-rooms/{roomId}/rankings")
    public ResponseEntity<ApiResponse<List<TeamRankingResponse>>> getRankings(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.getRankings(roomId)));
    }

    @GetMapping("/teams/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.ok(gameRoomService.getTeamMembers(teamId)));
    }

    @DeleteMapping("/game-rooms/{roomId}/members/{userId}/kick")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            Authentication authentication,
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        Long hostUserId = (Long) authentication.getPrincipal();
        gameRoomService.kickMember(hostUserId, roomId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
