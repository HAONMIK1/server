package kr.hhplus.be.server.order.presentation.controller.dto;



import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;


public class OrderResponse {
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
        ) {

        }
}