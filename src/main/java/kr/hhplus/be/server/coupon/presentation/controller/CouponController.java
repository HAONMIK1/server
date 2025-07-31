package kr.hhplus.be.server.coupon.presentation.controller;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/api/v1/coupons")
    public ResponseEntity<List<CouponResponse.CouponInfo>> getAllCoupons() {
        List<CouponEntity> coupons = couponService.getAllCoupons();
        List<CouponResponse.CouponInfo> response = coupons.stream()
                .map(CouponResponse.CouponInfo::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/users/{userId}/coupons/{couponId}/issue")
    public ResponseEntity<CouponResponse.Issue> issueCoupon(
            @PathVariable Long userId,
            @PathVariable Long couponId
    ) {
        CouponResponse.Issue userCoupon = couponService.issueCoupon(userId, couponId);
        return ResponseEntity.ok(userCoupon);
    }

    @GetMapping("/api/v1/users/{userId}/coupons")
    public ResponseEntity<List<CouponResponse.UserCouponInfo>> getUserCoupons(
            @PathVariable Long userId
    ) {
        List<UserCouponEntity> userCoupons = couponService.getUserCoupons(userId);
        List<CouponResponse.UserCouponInfo> response = userCoupons.stream()
                .map(CouponResponse.UserCouponInfo::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}