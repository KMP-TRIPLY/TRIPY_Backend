package com.kmp.Triply.domain.reward.repository;

import com.kmp.Triply.domain.reward.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    /** 사용자에게 읽어 주는 코드라 헷갈리는 글자(I·O·0·1)는 뺐다. */
    String COUPON_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    int COUPON_CODE_LENGTH = 8;
    SecureRandom COUPON_CODE_RANDOM = new SecureRandom();

    List<UserCoupon> findByUserIdOrderByIssuedAtDesc(Long userId);

    Optional<UserCoupon> findByCouponIdAndUserIdAndGameRoomId(Long couponId, Long userId, Long gameRoomId);

    boolean existsByCouponCode(String couponCode);

    long countByCouponId(Long couponId);

    /** 중복 없는 쿠폰 코드. 유일성 검사가 이 테이블에 있으니 발급 코드 생성도 여기 둔다. */
    default String nextUniqueCouponCode() {
        String couponCode;
        do {
            StringBuilder builder = new StringBuilder("TRIPLY-");
            for (int index = 0; index < COUPON_CODE_LENGTH; index++) {
                builder.append(COUPON_CODE_CHARACTERS.charAt(
                        COUPON_CODE_RANDOM.nextInt(COUPON_CODE_CHARACTERS.length())));
            }
            couponCode = builder.toString();
        } while (existsByCouponCode(couponCode));
        return couponCode;
    }
}
