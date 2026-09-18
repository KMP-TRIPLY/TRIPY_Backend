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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final GameRoomRepository gameRoomRepository;

    public List<UserCouponResponse> getMyCoupons(Long userId) {
        return userCouponRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(UserCouponResponse::from)
                .toList();
    }

    @Transactional
    public CouponIssueResponse issueCoupon(CouponIssueRequest request) {
        Coupon coupon = couponRepository.findByIdForUpdate(request.getCouponId())
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
                .couponCode(userCouponRepository.nextUniqueCouponCode())
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
}
