package com.kmp.Triply.domain.ranking.entity;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "rankings",
    indexes = {
        @Index(name = "idx_rankings_room_team", columnList = "game_room_id, team_id", unique = true),
        @Index(name = "idx_rankings_room_rank", columnList = "game_room_id, rank")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, columnDefinition = "smallint")
    private short rank;

    @Column(name = "final_score", nullable = false)
    private int finalScore = 0;

    @Column(name = "mission_clear_count", nullable = false, columnDefinition = "smallint default 0")
    private short missionClearCount = 0;

    @Column(name = "hint_used_count", nullable = false, columnDefinition = "smallint default 0")
    private short hintUsedCount = 0;

    @Column(name = "elapsed_seconds")
    private Integer elapsedSeconds;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Builder
    private Ranking(GameRoom gameRoom, Team team, short rank, int finalScore,
                    short missionClearCount, short hintUsedCount, Integer elapsedSeconds) {
        this.gameRoom = gameRoom;
        this.team = team;
        this.rank = rank;
        this.finalScore = finalScore;
        this.missionClearCount = missionClearCount;
        this.hintUsedCount = hintUsedCount;
        this.elapsedSeconds = elapsedSeconds;
        this.recordedAt = LocalDateTime.now();
    }
}