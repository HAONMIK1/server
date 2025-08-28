package kr.hhplus.be.server.coupon.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CouponQueueWorker {
    private final CouponService couponService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY_PREFIX = "coupon:queue:";
    private static final String ACTIVE_COUPONS_KEY = "coupon:active";

    @Scheduled(fixedDelay = 300)
    public void pollQueues() {
        var activeCouponIds = redisTemplate.opsForSet().members(ACTIVE_COUPONS_KEY);
        if (activeCouponIds == null || activeCouponIds.isEmpty()) return;
        for (String couponIdStr : activeCouponIds) {
            final long couponId = Long.parseLong(couponIdStr);
            final String queueKey = QUEUE_KEY_PREFIX + couponId;
            while (true) {
                String userIdStr = redisTemplate.opsForList().leftPop(queueKey, 5, TimeUnit.SECONDS);
                if (userIdStr == null) break;
                try {
                    couponService.issueOneReserved(Long.valueOf(userIdStr), couponId);
                } catch (Exception e) {
                    redisTemplate.opsForList().rightPush(queueKey, userIdStr);
                    break;
                }
            }
        }
    }
}


