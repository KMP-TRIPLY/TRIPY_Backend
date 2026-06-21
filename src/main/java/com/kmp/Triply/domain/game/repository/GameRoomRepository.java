package com.kmp.Triply.domain.game.repository;

import com.kmp.Triply.domain.game.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    Optional<GameRoom> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);
}
