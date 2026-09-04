package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameMode;
import com.kmp.Triply.domain.game.entity.GameRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 참여할 방을 고르는 목록의 한 줄. 방에 들어가려면 roomId 만 있으면 된다. */
@Getter
@Builder
@Schema(description = "대기 중인 게임방 목록 항목")
public class GameRoomSummaryResponse {

    @Schema(description = "게임방 ID. 참여할 때 이 값을 쓴다", example = "10")
    private Long roomId;

    @Schema(description = "방 이름", example = "공주 원정대")
    private String roomName;

    @Schema(description = "코스 ID", example = "1")
    private Long courseId;

    @Schema(description = "코스 제목", example = "백제의 잃어버린 무기고")
    private String courseTitle;

    @Schema(description = "게임 모드. 정원이 1이면 SOLO", example = "TEAM")
    private GameMode gameMode;

    @Schema(description = "현재 인원", example = "2")
    private long memberCount;

    @Schema(description = "정원", example = "6")
    private short maxMembers;

    @Schema(description = "정원이 찼는지. true 면 참여할 수 없다", example = "false")
    private boolean full;

    @Schema(description = "방 생성 시각")
    private LocalDateTime createdAt;

    public static GameRoomSummaryResponse of(GameRoom room, String roomName, long memberCount) {
        return GameRoomSummaryResponse.builder()
                .roomId(room.getId())
                .roomName(roomName)
                .courseId(room.getCourse().getId())
                .courseTitle(room.getCourse().getTitle())
                .gameMode(room.getGameMode())
                .memberCount(memberCount)
                .maxMembers(room.getMaxMembers())
                .full(memberCount >= room.getMaxMembers())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
