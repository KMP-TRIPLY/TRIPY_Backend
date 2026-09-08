package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    Optional<Reward> findByName(String name);

    boolean existsByName(String name);
}
