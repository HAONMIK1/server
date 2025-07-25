package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {
    long countByUserIdAndCouponId(Long userId, Long couponId);

    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
