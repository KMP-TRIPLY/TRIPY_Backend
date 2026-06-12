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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TeamRole role = TeamRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Builder
    private TeamMember(Team team, User user, TeamRole role) {
        this.team = team;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}