package kr.hhplus.be.server.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class SalesRankingResponse {
    public record Item(
            @Schema(description = "상품 ID", requiredMode = RequiredMode.REQUIRED)
            long productId,
            @Schema(description = "판매량", requiredMode = RequiredMode.REQUIRED)
            int salesCount
    ) {}
}


