package com.kmp.Triply.domain.game.controller;

import com.kmp.Triply.domain.game.dto.response.GameHistoryResponse;
import com.kmp.Triply.domain.game.service.GameHistoryService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Game History", description = "게임 참여 이력 API")
@RestController
@RequestMapping("/api/users/me/game-history")
@RequiredArgsConstructor
public class GameHistoryController {

    private final GameHistoryService gameHistoryService;

    @Operation(summary = "게임 참여 이력 조회", description = "현재 로그인한 사용자의 게임 참여 이력을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GameHistoryResponse>>> getMyGameHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(gameHistoryService.getMyGameHistory(userId)));
    }
}