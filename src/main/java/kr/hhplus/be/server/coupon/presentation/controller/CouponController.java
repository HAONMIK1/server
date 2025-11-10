package kr.hhplus.be.server.coupon.presentation.controller;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponEntity;
import kr.hhplus.be.server.coupon.presentation.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.application.service.CouponEventPublisher;
import kr.hhplus.be.server.coupon.application.event.CouponPublishRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponEventPublisher couponEventPublisher;

    @GetMapping("/api/v1/coupons")
    public ResponseEntity<List<CouponResponse.CouponInfo>> getAllCoupons() {
        List<CouponEntity> coupons = couponService.getAllCoupons();
        List<CouponResponse.CouponInfo> response = coupons.stream()
                .map(CouponResponse.CouponInfo::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/users/{userId}/coupons/{couponId}/issue")
    public ResponseEntity<String> issueCoupon(
            @PathVariable Long userId,
            @PathVariable Long couponId
    ) {
        // 카프카 이벤트 발행
        CouponPublishRequest request = CouponPublishRequest.builder()
                .couponId(couponId)
                .userId(userId)
                .requestTime(LocalDateTime.now())
                .build();
        
        couponEventPublisher.publishCouponRequest(request);
        
        return ResponseEntity.ok("쿠폰 발급 요청이 접수되었습니다. 처리 결과는 잠시 후 확인해주세요.");
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