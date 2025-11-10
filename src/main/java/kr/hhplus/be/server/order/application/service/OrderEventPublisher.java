package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.application.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String ORDER_COMPLETED_TOPIC = "order-completed";

    // 주문 완료 이벤트를 카프카로 발행
    public void publishOrderCompletedEvent(OrderCompletedEvent event) {
        String message = String.format(
            "주문 완료: 주문번호=%d, 사용자=%d, 금액=%d, 결제방법=%s, 완료시간=%s",
            event.getOrderId(),
            event.getUserId(),
            event.getFinalAmount(),
            event.getPaymentMethod(),
            event.getCompletedAt()
        );
        
        log.info("카프카로 주문 완료 메시지를 보냅니다: {}", message);
        kafkaTemplate.send(ORDER_COMPLETED_TOPIC, message);
    }
}
