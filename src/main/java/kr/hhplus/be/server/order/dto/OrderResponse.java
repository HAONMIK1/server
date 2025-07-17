package kr.hhplus.be.server.order.dto;



import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;


public class OrderResponse {
        public record Detail(
                @Schema(description = "주문 ID")
                long id,
                @Schema(description = "사용자 ID")
                long userId,
                @Schema(description = "사용한 쿠폰 ID")
                long userCouponId,
                @Schema(description = "총 주문 금액")
                int totalAmount,
                @Schema(description = "할인 금액")
                int discountAmount,
                @Schema(description = "최종 결제 금액")
                int finalAmount,
                @Schema(description = "주문 상태")
                String status,
                @Schema(description = "주문 시간")
                LocalDateTime orderTime,
                @Schema(description = "등록일")
                LocalDateTime regDt,
                @Schema(description = "수정일")
                LocalDateTime mdfcnDt,
                @Schema(description = "주문 아이템 목록")
                List<OrderItem> items,
                @Schema(description = "결제 정보") Payment payment
        ) {}

        public record OrderItem(
                @Schema(description = "주문 아이템 ID")
                long id,
                @Schema(description = "상품 ID")
                long productId,
                @Schema(description = "수량")
                int quantity,
                @Schema(description = "상품 가격")
                int price,
                @Schema(description = "등록일")
                LocalDateTime regDt
        ) {}

        public record Payment(
                @Schema(description = "결제 ID")
                long id,
                @Schema(description = "결제 금액")
                int paidAmount,
                @Schema(description = "결제 수단")
                String paymentMethod,
                @Schema(description = "결제 상태")
                String paymentStatus,
                @Schema(description = "결제 시간")
                LocalDateTime paidAt,
                @Schema(description = "등록일")
                LocalDateTime regDt
        ) {}
}