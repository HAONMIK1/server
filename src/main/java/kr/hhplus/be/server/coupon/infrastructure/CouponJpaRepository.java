package kr.hhplus.be.server.coupon.infrastructure;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    
    @Modifying
    @Query("UPDATE CouponEntity c SET c.issuedCount = c.issuedCount + 1 WHERE c.id = :couponId")
    void incrementIssuedCount(@Param("couponId") Long couponId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    Optional<CouponEntity> findById(Long id);
}