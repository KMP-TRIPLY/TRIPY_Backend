package com.kmp.Triply.domain.ranking.controller;

import com.kmp.Triply.domain.ranking.dto.request.RankingMode;
import com.kmp.Triply.domain.ranking.dto.response.RankingResponse;
import com.kmp.Triply.domain.ranking.service.RankingService;
import com.kmp.Triply.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking", description = "랭킹 API")
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "인게임 실시간 리더보드 조회", description = "게임룸의 팀전/개인전 실시간 랭킹을 조회합니다.")
    @GetMapping("/live")
    public ResponseEntity<ApiResponse<RankingResponse>> getLiveRankings(
            @RequestParam Long gameRoomId,
            @RequestParam(defaultValue = "TEAM") RankingMode mode) {
        return ResponseEntity.ok(ApiResponse.ok(rankingService.getLiveRankings(gameRoomId, mode)));
    }

    @Operation(summary = "코스별 최종 랭킹 조회", description = "코스 기준 종료된 게임의 팀전/개인전 최종 랭킹을 조회합니다.")
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<RankingResponse>> getCourseRankings(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "TEAM") RankingMode mode) {
        return ResponseEntity.ok(ApiResponse.ok(rankingService.getCourseRankings(courseId, mode)));
    }
}
