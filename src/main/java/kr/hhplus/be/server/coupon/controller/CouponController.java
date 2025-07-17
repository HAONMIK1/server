package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.coupon.dto.CouponResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/coupons")
public class CouponController {

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<CouponResponse.Issue> issueCoupon(
            @PathVariable Long userId,
            @PathVariable Long couponId
    ) {
        CouponResponse.Issue response = new CouponResponse.Issue(
                1L,
                userId,
                couponId,
                "신규가입 10% 할인쿠폰",
                10,
                10000,
                "미사용",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
        return ResponseEntity.ok(response);
    }

}