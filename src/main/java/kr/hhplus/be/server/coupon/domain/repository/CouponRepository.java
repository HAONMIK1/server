package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Optional<CouponEntity> findById(Long id);
    CouponEntity save(CouponEntity coupon);
    void incrementIssuedCount(Long couponId);
    List<CouponEntity> findAll();

    Optional<CouponEntity> findByIdLock(Long couponId);
}
