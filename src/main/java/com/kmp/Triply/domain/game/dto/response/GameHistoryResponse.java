package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.entity.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "게임 참여 이력 응답")
public class GameHistoryResponse {

    @Schema(description = "게임룸 ID", example = "1")
    private Long gameRoomId;

    @Schema(description = "코스 제목", example = "강릉 바다 여행 코스")
    private String courseTitle;

    @Schema(description = "팀 이름", example = "1조")
    private String teamName;

    @Schema(description = "팀 상태", example = "FINISHED")
    private TeamStatus teamStatus;

    @Schema(description = "게임 상태", example = "FINISHED")
    private GameStatus gameStatus;

    @Schema(description = "총 획득 점수", example = "1200")
    private int totalScore;

    @Schema(description = "순위", example = "1")
    private Short rank;

    @Schema(description = "참여일시")
    private LocalDateTime joinedAt;

    @Schema(description = "게임 시작일시")
    private LocalDateTime startedAt;

    @Schema(description = "게임 종료일시")
    private LocalDateTime endedAt;

    public static GameHistoryResponse from(TeamMember teamMember) {
        return GameHistoryResponse.builder()
                .gameRoomId(teamMember.getTeam().getGameRoom().getId())
                .courseTitle(teamMember.getTeam().getGameRoom().getCourse().getTitle())
                .teamName(teamMember.getTeam().getTeamName())
                .teamStatus(teamMember.getTeam().getStatus())
                .gameStatus(teamMember.getTeam().getGameRoom().getStatus())
                .totalScore(teamMember.getTeam().getTotalScore())
                .rank(teamMember.getTeam().getRank())
                .joinedAt(teamMember.getJoinedAt())
                .startedAt(teamMember.getTeam().getGameRoom().getStartedAt())
                .endedAt(teamMember.getTeam().getGameRoom().getEndedAt())
                .build();
    }
}