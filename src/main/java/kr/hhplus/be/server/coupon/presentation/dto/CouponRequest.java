package kr.hhplus.be.server.coupon.presentation.dto;

public class CouponRequest {
    
    public record Issue(Long couponId) {
    }
    
    public record Use(Long couponId) {
    }
}