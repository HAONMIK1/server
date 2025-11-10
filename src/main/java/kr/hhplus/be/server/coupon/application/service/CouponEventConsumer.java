package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponEventConsumer {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    
    private final ConcurrentHashMap<Long, AtomicInteger> successCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> failCount = new ConcurrentHashMap<>();

    @KafkaListener(topics = "coupon-publish-request", groupId = "coupon-consumer-group")
    public void receiveCouponRequest(String message) {
        log.info("쿠폰 발급 요청을 받았습니다: {}", message);
        processCouponRequest(message);
    }
    
    @Transactional
    private void processCouponRequest(String message) {
        try {
            String[] parts = message.split("쿠폰번호=")[1].split(",");
            Long couponId = Long.parseLong(parts[0].trim());
            Long userId = Long.parseLong(parts[1].split("=")[1].trim());
            
            successCount.putIfAbsent(couponId, new AtomicInteger(0));
            failCount.putIfAbsent(couponId, new AtomicInteger(0));
            
            try {
                // 1. 쿠폰 조회 및 발급 가능 여부 확인
                CouponEntity coupon = couponRepository.findById(couponId)
                        .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));
                
                // 2. 중복 발급 확인
                if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
                    throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
                }
                
                // 3. 쿠폰 발급 가능 여부 검증
                coupon.canIssue();
                
                // 4. UserCouponEntity 생성 및 저장
                UserCouponEntity userCoupon = UserCouponEntity.create(userId, couponId);
                userCouponRepository.save(userCoupon);
                
                // 5. 쿠폰 발급 수량 증가
                coupon.increaseIssuedCount();
                couponRepository.save(coupon);
                
                // 발급 성공
                int success = successCount.get(couponId).incrementAndGet();
                log.info(" 쿠폰 발급 성공: 쿠폰번호={}, 사용자={}, 남은재고={}, 성공수={}",
                    couponId, userId, coupon.getQuantity() - coupon.getIssuedCount(), success);
                
            } catch (IllegalArgumentException e) {
                // 발급 실패 (재고 부족, 중복 발급, 또는 기타 조건 불만족)
                int fail = failCount.get(couponId).incrementAndGet();
                log.info(" 쿠폰 발급 실패: 쿠폰번호={}, 사용자={}, 사유={}, 실패수={}",
                    couponId, userId, e.getMessage(), fail);
            }
            
        } catch (Exception e) {
            log.error("쿠폰 발급 처리 중 오류 발생: {}", message, e);
        }
    }

}
