package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
    
    // 사용자와 쿠폰 ID로 발급 여부 확인
    @Query("SELECT uc FROM UserCouponEntity uc WHERE uc.userId = :userId AND uc.couponId = :couponId")
    Optional<UserCouponEntity> findByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    List<UserCouponEntity> findByUserId(Long userId);
    
    long countByCouponId(Long couponId);
}