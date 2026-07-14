package com.kmp.Triply.domain.reward.service;

import com.kmp.Triply.domain.game.entity.GameRoom;
import com.kmp.Triply.domain.game.repository.GameRoomRepository;
import com.kmp.Triply.domain.reward.dto.request.CouponIssueRequest;
import com.kmp.Triply.domain.reward.dto.response.CouponIssueResponse;
import com.kmp.Triply.domain.reward.dto.response.UserCouponResponse;
import com.kmp.Triply.domain.reward.entity.Coupon;
import com.kmp.Triply.domain.reward.entity.UserCoupon;
import com.kmp.Triply.domain.reward.repository.CouponRepository;
import com.kmp.Triply.domain.reward.repository.UserCouponRepository;
import com.kmp.Triply.domain.user.entity.User;
import com.kmp.Triply.domain.user.repository.UserRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private static final String COUPON_CODE_PREFIX = "TRIPLY";
    private static final String COUPON_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int COUPON_CODE_LENGTH = 8;

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final GameRoomRepository gameRoomRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public List<UserCouponResponse> getMyCoupons(Long userId) {
        return userCouponRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(UserCouponResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CouponIssueResponse issueCoupon(CouponIssueRequest request) {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        validateIssuableCoupon(coupon);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GameRoom gameRoom = getGameRoom(request.getGameRoomId());

        if (gameRoom != null) {
            Optional<UserCoupon> issuedCoupon = userCouponRepository.findByCouponIdAndUserIdAndGameRoomId(
                    coupon.getId(), user.getId(), gameRoom.getId());
            if (issuedCoupon.isPresent()) {
                return CouponIssueResponse.of(false, UserCouponResponse.from(issuedCoupon.get()));
            }
        }

        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .gameRoom(gameRoom)
                .couponCode(generateCouponCode())
                .expiresAt(coupon.getValidUntil())
                .build());

        return CouponIssueResponse.of(true, UserCouponResponse.from(userCoupon));
    }

    private void validateIssuableCoupon(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActive()
                || now.isBefore(coupon.getValidFrom())
                || now.isAfter(coupon.getValidUntil())) {
            throw new CustomException(ErrorCode.COUPON_NOT_ISSUABLE);
        }
        if (coupon.getMaxIssueCount() != null
                && userCouponRepository.countByCouponId(coupon.getId()) >= coupon.getMaxIssueCount()) {
            throw new CustomException(ErrorCode.COUPON_NOT_ISSUABLE);
        }
    }

    private GameRoom getGameRoom(Long gameRoomId) {
        if (gameRoomId == null) {
            return null;
        }
        return gameRoomRepository.findById(gameRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_ROOM_NOT_FOUND));
    }

    private String generateCouponCode() {
        String couponCode;
        do {
            StringBuilder builder = new StringBuilder(COUPON_CODE_PREFIX.length() + 1 + COUPON_CODE_LENGTH);
            builder.append(COUPON_CODE_PREFIX).append("-");
            for (int index = 0; index < COUPON_CODE_LENGTH; index++) {
                builder.append(COUPON_CODE_CHARACTERS.charAt(secureRandom.nextInt(COUPON_CODE_CHARACTERS.length())));
            }
            couponCode = builder.toString().toUpperCase(Locale.ROOT);
        } while (userCouponRepository.existsByCouponCode(couponCode));
        return couponCode;
    }
}
