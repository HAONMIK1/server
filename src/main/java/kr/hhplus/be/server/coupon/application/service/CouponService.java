package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        coupon.canIssue();

        // 중복 발급 확인
        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        // 쿠폰 발급
        coupon.increaseIssuedCount();
        couponRepository.save(coupon);

        // 사용자 쿠폰 생성
        UserCouponEntity userCoupon = UserCouponEntity.create(userId, couponId);
        UserCouponEntity savedUserCoupon = userCouponRepository.save(userCoupon);

        return CouponResponse.Issue.from(savedUserCoupon);
    }

    public int calculateDiscount(Long userCouponId, int totalAmount) {
        UserCouponEntity userCoupon  = getUserCoupon(userCouponId);
        userCoupon.canUse();
        CouponEntity coupon = getCoupon(userCouponId);
        return coupon.calculateDiscount(totalAmount);
    }

    public void useCoupon(Long userId) {
        // 사용자의 미사용 쿠폰 중 첫 번째 쿠폰 사용
        List<UserCouponEntity> userCoupons = userCouponRepository.findByUserId(userId);
        UserCouponEntity userCoupon = userCoupons.stream()
                .filter(coupon -> coupon.getStatus() == UserCouponEntity.UserCouponStatus.UNUSED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("발급받지 않은 쿠폰입니다."));
        
        userCoupon.use();
        userCouponRepository.save(userCoupon);
    }

    public List<CouponEntity> getAllCoupons() {
        return couponRepository.findAll();
    }

    public UserCouponEntity getUserCoupon(Long userCouponId) {
        return userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 쿠폰을 찾을 수 없습니다."));
    }

    public CouponEntity getCoupon(Long userCouponId) {
        return couponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }

    public List<UserCouponEntity> getUserCoupons(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }
}
