package com.kmp.Triply.domain.reward.entity;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_rewards",
    indexes = {
        @Index(name = "idx_user_rewards_user_reward", columnList = "user_id, reward_id", unique = true),
        @Index(name = "idx_user_rewards_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Builder
    private UserReward(User user, Reward reward, GameRoom gameRoom) {
        this.user = user;
        this.reward = reward;
        this.gameRoom = gameRoom;
        this.earnedAt = LocalDateTime.now();
    }
}