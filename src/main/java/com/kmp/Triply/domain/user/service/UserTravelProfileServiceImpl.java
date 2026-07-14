package com.kmp.Triply.domain.user.service;

import com.kmp.Triply.domain.user.dto.request.UserTravelProfileRequest;
import com.kmp.Triply.domain.user.dto.response.UserTravelProfileResponse;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.entity.UserTravelProfile;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.domain.user.repository.UserTravelProfileRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserTravelProfileServiceImpl implements UserTravelProfileService {

    private final UserTravelProfileRepository userTravelProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserTravelProfileResponse getMyTravelProfile(Long userId) {
        // 프로필 미저장 유저는 404 대신 200 + data:null 반환 (신규 유저 정상 케이스)
        return userTravelProfileRepository.findByUserId(userId)
                .map(UserTravelProfileResponse::from)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserTravelProfileResponse saveMyTravelProfile(Long userId, UserTravelProfileRequest request) {
        UserTravelProfile profile = userTravelProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    return userTravelProfileRepository.save(UserTravelProfile.builder()
                            .user(user)
                            .styleType(request.getStyleType())
                            .moveType(request.getMoveType())
                            .companionType(request.getCompanionType())
                            .build());
                });

        profile.updateStyleType(request.getStyleType());
        profile.updatePreferences(request.getMoveType(), request.getCompanionType());
        profile.updateScores(request.getScoreHistory(), request.getScoreAdventure(),
                request.getScoreFood(), request.getScoreHealing(), request.getScoreCulture());

        return UserTravelProfileResponse.from(profile);
    }
}