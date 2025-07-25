package kr.hhplus.be.server.coupon.presentation.controller;

import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<CouponResponse.Issue> issueCoupon(
            @PathVariable Long userId,
            @PathVariable Long couponId
    ) {
            CouponResponse.Issue userCoupon = couponService.issueCoupon(userId, couponId);
            return ResponseEntity.ok(userCoupon);
    }

}