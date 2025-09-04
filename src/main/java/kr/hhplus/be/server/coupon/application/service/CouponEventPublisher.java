package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.application.event.CouponPublishRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String COUPON_PUBLISH_TOPIC = "coupon-publish-request";

    public void publishCouponRequest(CouponPublishRequest request) {
        String message = String.format(
            "쿠폰 발급 요청: 쿠폰번호=%d, 사용자=%d, 요청시간=%s",
            request.getCouponId(),
            request.getUserId(),
            request.getRequestTime()
        );
        
        String key = String.valueOf(request.getCouponId());
        
        log.info("카프카로 쿠폰 발급 요청을 보냅니다: {}", message);
        kafkaTemplate.send(COUPON_PUBLISH_TOPIC, key, message);
    }
}
