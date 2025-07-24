package kr.hhplus.be.server.product.presentation.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class ProductResponse {
    public record Details(
            @Schema(description = "상품 ID", requiredMode = RequiredMode.REQUIRED)
            Long id,
            @Schema(description = "상품 이름", requiredMode = RequiredMode.REQUIRED)
            String name,
            @Schema(description = "상품 가격", requiredMode = RequiredMode.REQUIRED)
            int price,
            @Schema(description = "총 수량", requiredMode = RequiredMode.REQUIRED)
            int totalQuantity,
            @Schema(description = "현재 재고", requiredMode = RequiredMode.REQUIRED)
            int stockQuantity,
            @Schema(description = "상품 상태", requiredMode = RequiredMode.REQUIRED)
            String status
    ) {}

}