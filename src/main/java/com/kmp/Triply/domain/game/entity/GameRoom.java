package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.course.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "game_rooms",
    indexes = {
        @Index(name = "idx_game_rooms_status", columnList = "status"),
        @Index(name = "idx_game_rooms_room_code", columnList = "room_code", unique = true)
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "room_code", nullable = false, unique = true, length = 8)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false, length = 10)
    private GameMode gameMode = GameMode.TEAM;

    @Column(name = "max_teams", nullable = false, columnDefinition = "smallint default 4")
    private short maxTeams = 4;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private GameRoom(Course course, String roomCode, GameMode gameMode, short maxTeams) {
        this.course = course;
        this.roomCode = roomCode;
        this.gameMode = gameMode;
        this.maxTeams = maxTeams;
    }

    public void start() {
        this.status = GameStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void finish() {
        this.status = GameStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = GameStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }
}