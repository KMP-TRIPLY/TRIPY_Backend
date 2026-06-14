package com.kmp.Triply.domain.user.repository;

import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    boolean existsByEmail(String email);
}