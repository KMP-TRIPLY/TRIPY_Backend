package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.TeamLeaveHistory;
import com.kmp.Triply.domain.game.entity.TeamLeaveReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "팀 탈퇴 응답")
public class TeamLeaveResponse {

    @Schema(description = "탈퇴 기록 ID", example = "1")
    private Long leaveHistoryId;

    @Schema(description = "팀 ID", example = "4")
    private Long teamId;

    @Schema(description = "탈퇴 사용자 ID", example = "3")
    private Long userId;

    @Schema(description = "탈퇴 사유 유형", example = "PERSONAL_REASON")
    private TeamLeaveReasonType reasonType;

    @Schema(description = "재분배 대상 보존 점수", example = "300")
    private int preservedScore;

    public static TeamLeaveResponse from(TeamLeaveHistory history) {
        return TeamLeaveResponse.builder()
                .leaveHistoryId(history.getId())
                .teamId(history.getTeam().getId())
                .userId(history.getUser().getId())
                .reasonType(history.getReasonType())
                .preservedScore(history.getPreservedScore())
                .build();
    }
}
