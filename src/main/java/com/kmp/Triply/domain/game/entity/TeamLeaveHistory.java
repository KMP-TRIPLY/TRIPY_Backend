package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "team_leave_histories",
    indexes = {
        @Index(name = "idx_team_leave_histories_room_team", columnList = "game_room_id, team_id"),
        @Index(name = "idx_team_leave_histories_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamLeaveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 30)
    private TeamLeaveReasonType reasonType;

    @Column(name = "reason_detail", length = 500)
    private String reasonDetail;

    @Column(name = "preserved_score", nullable = false)
    private int preservedScore = 0;

    @Column(name = "left_at", nullable = false)
    private LocalDateTime leftAt;

    @Builder
    private TeamLeaveHistory(GameRoom gameRoom, Team team, User user, TeamLeaveReasonType reasonType,
                             String reasonDetail, int preservedScore) {
        this.gameRoom = gameRoom;
        this.team = team;
        this.user = user;
        this.reasonType = reasonType;
        this.reasonDetail = reasonDetail;
        this.preservedScore = preservedScore;
        this.leftAt = LocalDateTime.now();
    }
}
