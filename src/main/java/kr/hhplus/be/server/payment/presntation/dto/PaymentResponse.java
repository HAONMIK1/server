package kr.hhplus.be.server.payment.presntation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;

import java.time.LocalDateTime;

public record PaymentResponse() {
    
    public record Complete(
            PaymentDetail payment
    ) {}
    
    public record PaymentDetail(
            Long paymentId,
            Long orderId,
            Integer paidAmount,
            String paymentMethod,
            PaymentEntity.PaymentStatus paymentStatus,
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