package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.course.entity.Course;
import com.kmp.Triply.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User host;

    @Column(name = "room_code", nullable = false, unique = true, length = 8)
    private String roomCode;

    /**
     * 비밀번호 해시. null 이면 잠기지 않은 방이라 목록에서 바로 들어올 수 있다.
     * 걸어두면 아는 사람만 참여할 수 있다.
     */
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false, length = 10)
    private GameMode gameMode = GameMode.TEAM;

    @Column(name = "max_members", nullable = false, columnDefinition = "smallint default 4")
    private short maxMembers = 4;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "ready_since_at")
    private LocalDateTime readySinceAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private GameRoom(Course course, User host, String roomCode, String passwordHash, GameMode gameMode, short maxMembers) {
        this.course = course;
        this.host = host;
        this.roomCode = roomCode;
        this.passwordHash = passwordHash;
        this.gameMode = gameMode;
        this.maxMembers = maxMembers;
    }

    /** 비밀번호가 걸린 방인지. 목록에 자물쇠를 보여주고, 참여 때 비밀번호를 받을지 정한다. */
    public boolean isLocked() {
        return passwordHash != null;
    }

    public void changeCourse(Course course) {
        this.course = course;
    }

    public void changeHost(User host, LocalDateTime delegatedAt) {
        this.host = host;
        this.readySinceAt = delegatedAt;
    }

    public void markReady(LocalDateTime readyAt) {
        if (this.readySinceAt == null) {
            this.readySinceAt = readyAt;
        }
    }

    public void clearReady() {
        this.readySinceAt = null;
    }

    /**
     * 정원 변경. 모드는 정원에서 유도하므로 함께 갱신한다 —
     * 따로 두면 "정원 1 인데 TEAM" 처럼 어긋난 상태가 생긴다.
     */
    public void changeMaxMembers(short maxMembers) {
        this.maxMembers = maxMembers;
        this.gameMode = maxMembers == 1 ? GameMode.SOLO : GameMode.TEAM;
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
