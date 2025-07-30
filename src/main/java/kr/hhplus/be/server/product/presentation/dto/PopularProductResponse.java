package kr.hhplus.be.server.product.presentation.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;

import java.time.LocalDateTime;

public class PopularProductResponse {
    public record Details(
            @Schema(description = "인기상품 ID", requiredMode = RequiredMode.REQUIRED)
            long id,
            @Schema(description = "상품 ID", requiredMode = RequiredMode.REQUIRED)
            long productId,
            @Schema(description = "상품 이름", requiredMode = RequiredMode.REQUIRED)
            String productName,
            @Schema(description = "상품 가격", requiredMode = RequiredMode.REQUIRED)
            int productPrice,
            @Schema(description = "판매량", requiredMode = RequiredMode.REQUIRED)
            int salesCount,
            @Schema(description = "조회수", requiredMode = RequiredMode.REQUIRED)
            int viewCount,
            @Schema(description = "등록일", requiredMode = RequiredMode.REQUIRED, format = "date-time")
            LocalDateTime regDt

    ) {
        public static Details from(PopularProductEntity popularProduct) {
            return new Details(
                    popularProduct.getId(),
                    popularProduct.getProductId(),
                    popularProduct.getProduct() != null ? popularProduct.getProduct().getName() : "상품명 없음",
                    popularProduct.getProduct() != null ? popularProduct.getProduct().getPrice() : 0,
                    popularProduct.getSalesCount(),
                    popularProduct.getViewCount(),
                    popularProduct.getRegDt()
            );
        }
    }

}