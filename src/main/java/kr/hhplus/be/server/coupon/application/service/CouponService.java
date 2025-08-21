package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY_PREFIX = "coupon:queue:";
    private static final String ORDER_KEY_PREFIX = "coupon:order:";

    @Transactional
    public CouponResponse.Issue issueCoupon(Long userId, Long couponId) {
        //DB락
        //CouponEntity coupon = couponRepository.findByIdLock(couponId)
        //       .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        // 쿠폰 조회
        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));


        // 발급 가능 여부 확인
        coupon.canIssue();

        // 중복 발급 확인
        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }

        long remaining = coupon.getQuantity() - coupon.getIssuedCount();

        // 대기열 길이를 잔여 재고량으로 제한
        String queueKey = QUEUE_KEY_PREFIX + couponId;
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        if (queueSize == null) queueSize = 0L;
        if (queueSize >= remaining) {
            throw new IllegalArgumentException("선착순이 마감되었습니다.");
        }

        // 대기열 등록
        redisTemplate.opsForList().rightPush(queueKey, String.valueOf(userId));

        // 비동기 처리
        processQueueAsync(couponId);

        return CouponResponse.Issue.fromQueue(userId, couponId, "ISSUED");
    }

    @Async
    public CompletableFuture<Void> processQueueAsync(Long couponId) {
        try {
            processQueue(couponId);
        } catch (Exception ignored) {
        }
        return CompletableFuture.completedFuture(null);
    }

    private void processQueue(Long couponId) {
        final String queueKey = QUEUE_KEY_PREFIX + couponId;
        final String orderKey = ORDER_KEY_PREFIX + couponId;

        while (true) {
            String userIdStr = redisTemplate.opsForList().leftPop(queueKey);
            if (userIdStr == null) break;

            Long userId = Long.valueOf(userIdStr);

            try {
                issueOneReserved(userId, couponId);

                // 발급 순서 기록
                redisTemplate.opsForZSet().add(orderKey, userIdStr, System.currentTimeMillis());
            } catch (Exception e) {
                // 에러 시 재시도 위해 큐 끝에 재삽입
                redisTemplate.opsForList().rightPush(queueKey, userIdStr);
            }
        }
    }

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
