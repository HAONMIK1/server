package kr.hhplus.be.server.coupon.presentation.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;

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
                @Schema(description = "발급 시간", requiredMode = RequiredMode.REQUIRED)
                LocalDateTime issuedAt
        ) {
                public static Issue from(UserCouponEntity entity) {
                        return new Issue(
                                entity.getId(),
                                entity.getUserId(),
                                entity.getCouponId(),
                                entity.getStatus().name(),
                                entity.getRegDt()
                        );
                }

        }
}