package kr.hhplus.be.server.product.presentation.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDateTime;

public class PopularProductResponse {
    public record Details(
            @Schema(description = "상품 ID", requiredMode = RequiredMode.REQUIRED)
            long id,
            @Schema(description = "상품 이름", requiredMode = RequiredMode.REQUIRED)
            String name,
            @Schema(description = "판매량", requiredMode = RequiredMode.REQUIRED)
            int salesCount,
            @Schema(description = "조회수", requiredMode = RequiredMode.REQUIRED)
            int viewCount,
            @Schema(description = "등록일", requiredMode = RequiredMode.REQUIRED, format = "date-time")
            LocalDateTime regDt

    ) {}

}