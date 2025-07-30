package kr.hhplus.be.server.payment.presntation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;

import java.time.LocalDateTime;

public class PaymentResponse {
        @Schema(name = "PaymentResponse.Result", description = "결제 결과")
        public record Result(
                @Schema(description = "결제 ID", required = true)
                Long paymentId,
                @Schema(description = "주문 ID", required = true)
                Long orderId,
                @Schema(description = "최종 결제 금액", required = true)
                int finalAmount,
                @Schema(description = "결제 상태", required = true)
                PaymentEntity.PaymentStatus paymentStatus
        ) {}
        
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Complete(
                PaymentDetail payment
        ) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PaymentDetail(
                @Schema(description = "결제 ID", required = true)
                Long paymentId,
                @Schema(description = "주문 ID", required = true)
                Long orderId,
                @Schema(description = "결제 금액", required = true)
                int paidAmount,
                @Schema(description = "결제 방법", required = true)
                String paymentMethod,
                @Schema(description = "결제 상태", required = true)
                PaymentEntity.PaymentStatus paymentStatus,
                @Schema(description = "결제 일시", required = true)
                LocalDateTime paidAt
        ) {
            public static PaymentDetail from(PaymentEntity payment) {
                return new PaymentDetail(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getPaidAmount(),
                        payment.getPaymentMethod().name(),
                        payment.getPaymentStatus(),
                        payment.getPaidAt()
                );
            }
        }
}