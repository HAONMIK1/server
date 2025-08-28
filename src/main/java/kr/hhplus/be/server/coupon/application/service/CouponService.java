package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY_PREFIX = "coupon:queue:";
    private static final String STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String USERS_KEY_PREFIX = "coupon:users:";
    private static final String ACTIVE_COUPONS_KEY = "coupon:active";

    @Transactional
    public CouponResponse.Issue issueCoupon(Long userId, Long couponId) {
        final String stockKey = STOCK_KEY_PREFIX + couponId;
        final String usersKey = USERS_KEY_PREFIX + couponId;
        final String queueKey = QUEUE_KEY_PREFIX + couponId;

        // 재고 키 초기화
        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            CouponEntity coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
            coupon.canIssue();
            long remaining = coupon.getQuantity() - coupon.getIssuedCount();
            redisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(remaining));
            // 활성 쿠폰 등록
            redisTemplate.opsForSet().add(ACTIVE_COUPONS_KEY, String.valueOf(couponId));
        }

        // 원자적 재고 차감
        Long newStock = redisTemplate.opsForValue().increment(stockKey, -1L);
        if (newStock == null || newStock < 0) {
            // 롤백
            redisTemplate.opsForValue().increment(stockKey, 1L);
            throw new IllegalArgumentException("선착순이 마감되었습니다.");
        }

        // 중복 방지
        Long added = redisTemplate.opsForSet().add(usersKey, String.valueOf(userId));
        if (added == null || added == 0L) {
            // 롤백
            redisTemplate.opsForValue().increment(stockKey, 1L);
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        // 큐 등록
        redisTemplate.opsForList().rightPush(queueKey, String.valueOf(userId));

        return CouponResponse.Issue.fromQueue(userId, couponId, UserCouponEntity.UserCouponStatus.ISSUED.name());
    }

    // 워커에서 사용: 단건 처리(트랜잭션)
    @Transactional
    protected void issueOneReserved(Long userId, Long couponId) {
        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        UserCouponEntity userCoupon = UserCouponEntity.create(userId, couponId);
        userCouponRepository.save(userCoupon);
        coupon.increaseIssuedCount();
        couponRepository.save(coupon);
    }

    @Transactional
    public int calculateDiscount(Long userCouponId, int totalAmount) {
        UserCouponEntity userCoupon  = getUserCoupon(userCouponId);
        userCoupon.canUse();
        CouponEntity coupon = getCoupon(userCouponId);
        return coupon.calculateDiscount(totalAmount);
    }

    @Transactional
    public void useCoupon(Long userId, Long couponId) {
        UserCouponEntity userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("발급받지 않은 쿠폰입니다."));

        userCoupon.use();
        userCouponRepository.save(userCoupon);
    }

    @Transactional
    public List<CouponEntity> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public UserCouponEntity getUserCoupon(Long userCouponId) {
        return userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public CouponEntity getCoupon(Long userCouponId) {
        return couponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public List<UserCouponEntity> getUserCoupons(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }


}
