package com.kmp.Triply.domain.user.repository;

import com.kmp.Triply.domain.user.entity.UserTravelProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTravelProfileRepository extends JpaRepository<UserTravelProfile, Long> {

    Optional<UserTravelProfile> findByUserId(Long userId);
}
