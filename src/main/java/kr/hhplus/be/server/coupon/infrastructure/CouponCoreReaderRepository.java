package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponCoreReaderRepository implements CouponRepository {
    
    private final CouponJpaRepository couponJpaRepository;
    
    @Override
    public Optional<CouponEntity> findById(Long id) {
        return couponJpaRepository.findById(id);
    }
    @Override
    public Optional<CouponEntity> findByIdLock(Long id) {
        return couponJpaRepository.findByIdLock(id);
    }
    @Override
    public CouponEntity save(CouponEntity coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public void incrementIssuedCount(Long couponId) {
        couponJpaRepository.incrementIssuedCount(couponId);
    }

    @Override
    public List<CouponEntity> findAll() {
        return couponJpaRepository.findAll();
    }

} 