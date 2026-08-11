package com.kmp.Triply.domain.user.repository;

import com.kmp.Triply.domain.user.entity.SocialProvider;
import com.kmp.Triply.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** 탈퇴한 계정은 제외한다. 탈퇴 후 같은 소셜 계정으로 재가입할 수 있어야 한다. */
    Optional<User> findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider socialProvider, String socialId);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmail(String email);
}
