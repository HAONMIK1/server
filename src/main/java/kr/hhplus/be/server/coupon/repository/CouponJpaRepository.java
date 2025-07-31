package kr.hhplus.be.server.coupon.repository;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    
    @Modifying
    @Query("UPDATE CouponEntity c SET c.issuedCount = c.issuedCount + 1 WHERE c.id = :couponId")
    void incrementIssuedCount(@Param("couponId") Long couponId);
    
}