package kr.hhplus.be.server.coupon.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDateTime;

public class CouponResponse {
        public record Issue(
                @Schema(description = "사용자 쿠폰 ID", requiredMode = RequiredMode.REQUIRED)
                long userCouponId,
                @Schema(description = "사용자 ID", requiredMode = RequiredMode.REQUIRED)
                long userId,
                @Schema(description = "쿠폰 ID", requiredMode = RequiredMode.REQUIRED)
                long couponId,
                @Schema(description = "쿠폰 이름", requiredMode = RequiredMode.REQUIRED)
                String couponName,
                @Schema(description = "할인율", requiredMode = RequiredMode.REQUIRED)
                int discountRate,
                @Schema(description = "최대 할인 금액", requiredMode = RequiredMode.REQUIRED)
                int maxAmount,
                @Schema(description = "쿠폰 상태", requiredMode = RequiredMode.REQUIRED)
                String status,
                @Schema(description = "발급 시간", requiredMode = RequiredMode.REQUIRED)
                LocalDateTime issuedAt,
                @Schema(description = "만료 시간", requiredMode = RequiredMode.REQUIRED)
                LocalDateTime expiresAt
        ) {}

}