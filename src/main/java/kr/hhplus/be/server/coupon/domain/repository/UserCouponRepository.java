package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
