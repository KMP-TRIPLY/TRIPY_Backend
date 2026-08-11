package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.request.UserUpdateRequest;
import com.kmp.Triply.domain.user.dto.response.UserProfileResponse;
import com.kmp.Triply.domain.user.dto.response.UserResponse;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.global.security.jwt.RefreshTokenStore;
import com.kmp.Triply.global.security.jwt.TokenBlacklist;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.getNickname(), request.getProfileImage());
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, String accessToken) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
        refreshTokenStore.delete(userId);
        // 이게 없으면 탈퇴 후에도 남은 유효기간 동안 다른 API 를 계속 호출할 수 있다
        tokenBlacklist.add(accessToken);
    }
}