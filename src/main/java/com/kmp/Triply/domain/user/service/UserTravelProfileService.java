package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.request.UserTravelProfileRequest;
import com.kmp.Triply.domain.user.dto.response.UserTravelProfileResponse;

public interface UserTravelProfileService {

    UserTravelProfileResponse getMyTravelProfile(Long userId);

    UserTravelProfileResponse saveMyTravelProfile(Long userId, UserTravelProfileRequest request);
}
