package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "teams",
    indexes = @Index(name = "idx_teams_game_room_id", columnList = "game_room_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leader;

    @Column(name = "team_name", nullable = false, length = 50)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TeamStatus status = TeamStatus.READY;

    @Column(name = "total_score", nullable = false)
    private int totalScore = 0;

    @Column(columnDefinition = "smallint")
    private Short rank;

    @Column(name = "hint_count_used", nullable = false, columnDefinition = "smallint default 0")
    private short hintCountUsed = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Team(GameRoom gameRoom, User leader, String teamName) {
        this.gameRoom = gameRoom;
        this.leader = leader;
        this.teamName = teamName;
    }

    public void addScore(int score) {
        this.totalScore += score;
    }

    public void useHint() {
        this.hintCountUsed++;
    }

    public void finish(short rank) {
        this.status = TeamStatus.FINISHED;
        this.rank = rank;
    }
}