package kr.hhplus.be.server.order.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventConsumer {

    // order-completed 토픽에서 메시지를 받는 메서드
    @KafkaListener(topics = "order-completed", groupId = "order-consumer-group")
    public void receiveOrderCompletedMessage(String message) {
        log.info("주문 완료 메시지를 받았습니다: {}", message);
        
        // 여기서 실제 비즈니스 로직을 처리합니다
        processOrderCompletedMessage(message);
    }
    
    // 메시지를 처리하는 메서드
    private void processOrderCompletedMessage(String message) {
        log.info("주문 완료 메시지를 처리합니다: {}", message);
        
        // 실제로는 여기서:
        // 1. 알림톡 발송
        // 2. 데이터베이스 업데이트
        // 3. 외부 API 호출
        // 등을 수행합니다
        
        log.info("✅ 주문 완료 처리가 완료되었습니다!");
    }
}
