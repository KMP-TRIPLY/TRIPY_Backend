package com.kmp.Triply.domain.ranking.entity;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "rankings",
    indexes = {
        @Index(name = "idx_rankings_room_type_rank", columnList = "game_room_id, ranking_type, rank"),
        @Index(name = "idx_rankings_room_team", columnList = "game_room_id, team_id"),
        @Index(name = "idx_rankings_room_user", columnList = "game_room_id, user_id")
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
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_type", nullable = false, length = 20)
    private RankingType rankingType = RankingType.ROOM;

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
    private Ranking(GameRoom gameRoom, Team team, User user, RankingType rankingType,
                    short rank, int finalScore,
                    short missionClearCount, short hintUsedCount, Integer elapsedSeconds) {
        this.gameRoom = gameRoom;
        this.team = team;
        this.user = user;
        this.rankingType = rankingType;
        this.rank = rank;
        this.finalScore = finalScore;
        this.missionClearCount = missionClearCount;
        this.hintUsedCount = hintUsedCount;
        this.elapsedSeconds = elapsedSeconds;
        this.recordedAt = LocalDateTime.now();
    }
}
