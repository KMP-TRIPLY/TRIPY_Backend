package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.GameMode;
import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.GameStatus;
import com.kmp.Triply.domain.game.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 내가 아직 끝내지 않은 방 한 줄.
 * 앱을 껐다 켜거나 기기를 바꿨을 때 roomId 를 몰라도 돌아올 수 있게 하는 것이 목적이라,
 * 목록 화면에 필요한 진행 상황까지 함께 담는다.
 */
@Getter
@Builder
@Schema(description = "내가 참여 중인 게임방")
public class ActiveGameRoomResponse {

    @Schema(description = "게임방 ID. 이 값으로 다시 참여한다", example = "10")
    private Long roomId;

    @Schema(description = "방 코드", example = "K7QP2M")
    private String roomCode;

    @Schema(description = "방 이름", example = "공주 원정대")
    private String roomName;

    @Schema(description = "방 상태. WAITING 이면 대기실, RUNNING 이면 진행 중", example = "RUNNING")
    private GameStatus status;

    @Schema(description = "팀 ID", example = "7")
    private Long teamId;

    @Schema(description = "코스 ID", example = "3")
    private Long courseId;

    @Schema(description = "코스 제목", example = "사비의 마지막 하루")
    private String courseTitle;

    @Schema(description = "게임 모드", example = "TEAM")
    private GameMode gameMode;

    @Schema(description = "현재 인원", example = "3")
    private long memberCount;

    @Schema(description = "정원", example = "4")
    private short maxMembers;

    @Schema(description = "내가 방장인지", example = "true")
    private boolean host;

    @Schema(description = "코스 전체 스팟 수", example = "3")
    private long totalSpots;

    @Schema(description = "우리 팀이 완료한 스팟 수", example = "1")
    private long completedSpots;

    @Schema(description = "게임 시작 시각. 아직 대기실이면 null")
    private LocalDateTime startedAt;

    @Schema(description = "방 생성 시각")
    private LocalDateTime createdAt;

    public static ActiveGameRoomResponse of(GameRoom room, Team team, Long userId,
                                            long memberCount, long totalSpots, long completedSpots) {
        return ActiveGameRoomResponse.builder()
                .roomId(room.getId())
                .roomCode(room.getRoomCode())
                .roomName(team.getTeamName())
                .status(room.getStatus())
                .teamId(team.getId())
                .courseId(room.getCourse().getId())
                .courseTitle(room.getCourse().getTitle())
                .gameMode(room.getGameMode())
                .memberCount(memberCount)
                .maxMembers(room.getMaxMembers())
                .host(room.getHost().getId().equals(userId))
                .totalSpots(totalSpots)
                .completedSpots(completedSpots)
                .startedAt(room.getStartedAt())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
