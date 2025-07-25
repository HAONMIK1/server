package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponResponse.Issue issueCoupon(Long userId, Long couponId) {
        // 쿠폰 조회
        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        // 발급 가능 여부 확인
        if (!coupon.canIssue()) {
            throw new IllegalArgumentException("쿠폰을 발급할 수 없습니다. (품절 또는 기간 만료)");
        }

        // 중복 발급 확인
        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        // 쿠폰 발급
        coupon.issue();
        couponRepository.save(coupon);

        // 사용자 쿠폰 생성
        UserCouponEntity userCoupon = UserCouponEntity.create(userId, couponId);
        UserCouponEntity savedUserCoupon = userCouponRepository.save(userCoupon);

        return CouponResponse.Issue.from(savedUserCoupon);
    }

    public int calculateDiscount(Long userCouponId, int totalAmount) {
        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 쿠폰을 찾을 수 없습니다."));

        if (!userCoupon.canUse()) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다.");
        }

        CouponEntity coupon = couponRepository.findById(userCoupon.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        return coupon.calculateDiscount(totalAmount);
    }

    public void useCoupon(Long userCouponId) {
        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 쿠폰을 찾을 수 없습니다."));

        userCoupon.use();
        userCouponRepository.save(userCoupon);
    }

}
