package kr.hhplus.be.server.payment.presntation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PaymentRequest {
    public record Pay(
            @Schema(description = "사용자 ID", required = true)
            Long userId
    ) {}
    
    public record Process(
            @Schema(description = "결제 방법", required = true)
            String paymentMethod
    ) {}
}