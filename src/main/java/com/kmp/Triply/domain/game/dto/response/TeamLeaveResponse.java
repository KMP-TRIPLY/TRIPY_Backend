package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.TeamLeaveHistory;
import com.kmp.Triply.domain.game.entity.TeamLeaveReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "게임방 하차 응답")
public class TeamLeaveResponse {

    @Schema(description = "하차 기록 ID", example = "1")
    private Long leaveHistoryId;

    @Schema(description = "게임방 ID", example = "10")
    private Long roomId;

    @Schema(description = "하차 사용자 ID", example = "3")
    private Long userId;

    @Schema(description = "하차 사유 유형", example = "PERSONAL_REASON")
    private TeamLeaveReasonType reasonType;

    @Schema(description = "재분배 대상 보존 점수", example = "300")
    private int preservedScore;

    /** 대기실 하차. 아직 시작 전이라 기록도 보존 점수도 없다. */
    public static TeamLeaveResponse ofWaitingRoom(Long roomId, Long userId) {
        return TeamLeaveResponse.builder()
                .roomId(roomId)
                .userId(userId)
                .preservedScore(0)
                .build();
    }

    public static TeamLeaveResponse from(TeamLeaveHistory history) {
        return TeamLeaveResponse.builder()
                .leaveHistoryId(history.getId())
                .roomId(history.getGameRoom().getId())
                .userId(history.getUser().getId())
                .reasonType(history.getReasonType())
                .preservedScore(history.getPreservedScore())
                .build();
    }
}
