package kr.hhplus.be.server.order.presentation.dto;



import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;

public class OrderResponse {
        public record Detail(
                Long orderId,
                Long userId,
                String status,
                int totalAmount,
                int discountAmount,
                int finalAmount,
                LocalDateTime orderTime
        ) {
                public static Detail from(OrderEntity order) {
                        return new Detail(
                                order.getId(),
                                order.getUserId(),
                                order.getStatus().name(),
                                order.getTotalAmount(),
                                order.getDiscountAmount(),
                                order.getFinalAmount(),
                                order.getOrderTime()
                        );
                }
        }

        public record Item(
                Long productId,
                String productName,
                int quantity,
                int price
        ) {}
}