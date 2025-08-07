package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCouponEntity> findById(Long id);
    UserCouponEntity save(UserCouponEntity userCoupon);
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long id);

    List<UserCouponEntity> findByUserId(Long userId);
    long countByCouponId(Long couponId);
}
