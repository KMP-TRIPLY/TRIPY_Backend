package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.request.UserUpdateRequest;
import com.kmp.Triply.domain.user.dto.response.UserResponse;

public interface UserService {

    UserResponse getUser(Long userId);

    UserResponse updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);
}