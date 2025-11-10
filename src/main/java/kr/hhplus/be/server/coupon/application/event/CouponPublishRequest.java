package kr.hhplus.be.server.coupon.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponPublishRequest {
    
    private Long couponId;
    private Long userId;
    private LocalDateTime requestTime;
}
