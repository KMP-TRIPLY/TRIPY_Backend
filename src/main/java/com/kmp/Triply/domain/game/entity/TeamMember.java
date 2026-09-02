package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "team_members",
    indexes = {
        @Index(name = "idx_team_members_team_user", columnList = "team_id, user_id", unique = true),
        @Index(name = "idx_team_members_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    private TeamMember(Team team, User user) {
        this.team = team;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
    }

    public void leave() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }

    /** 나갔던 멤버가 같은 방에 다시 들어온다. 팀은 원래대로 유지한다. */
    public void rejoin() {
        this.isActive = true;
        this.leftAt = null;
    }
}
