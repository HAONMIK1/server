package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCouponCoreReaderRepository implements UserCouponRepository {
    
    private final UserCouponJpaRepository userCouponJpaRepository;
    
    @Override
    public Optional<UserCouponEntity> findById(Long id) {
        return userCouponJpaRepository.findById(id);
    }
    
    @Override
    public UserCouponEntity save(UserCouponEntity userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }
    
    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).isPresent();
    }

    @Override
    public Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long id) {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId, id);
    }

    @Override
    public List<UserCouponEntity> findByUserId(Long userId) {
        return userCouponJpaRepository.findByUserId(userId);
    }
} 