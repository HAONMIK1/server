package kr.hhplus.be.server.order.presentation.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

public class OrderRequest {
    public record Create(
            @Schema(description = "주문 ID", requiredMode = RequiredMode.REQUIRED)
            Long orderId,
            @Schema(description = "사용할 쿠폰 ID", requiredMode = RequiredMode.NOT_REQUIRED)
            Long userCouponId
    ) {}

    public record OrderItem(
            @Schema(description = "상품 ID", requiredMode = RequiredMode.REQUIRED)
            Long productId,
            @Schema(description = "수량", requiredMode = RequiredMode.REQUIRED, minimum = "1")
            int quantity
    ) {}
}
