package kr.hhplus.be.server.order.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
    
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private Integer totalAmount;
    private Integer finalAmount;
    private Integer discountAmount;
    private String paymentMethod;
    private LocalDateTime completedAt;
}
