package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_result")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount = 0;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;


    public enum OrderStatus {
        PENDING,    // 대기
        COMPLETED,  // 완료
        FAILED      // 실패
    }

    public void calculateFinalAmount() {
        this.finalAmount = this.totalAmount - this.discountAmount;
    }

    public void applyCoupon(Long couponId, Integer discountAmount) {
        this.userCouponId = couponId;
        this.discountAmount = discountAmount;
        calculateFinalAmount();
    }

    public void completeOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("주문 상태가 대기 중이 아닙니다.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void failOrder() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 주문은 실패 처리할 수 없습니다.");
        }
        this.status = OrderStatus.FAILED;
    }

    public boolean canProcess() {
        return this.status == OrderStatus.PENDING && this.finalAmount > 0;
    }

    public boolean hasCoupon() {
        return this.userCouponId != null;
    }

    public static OrderEntity createOrder(Long userId, Integer totalAmount) {
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(0);
        order.setFinalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());
        order.setRegDt(LocalDateTime.now());
        order.setMdfcnDt(LocalDateTime.now());
        return order;
    }

    public static OrderEntity createOrderWithCoupon(Long userId, Integer totalAmount, Long couponId, Integer discountAmount) {
        OrderEntity order = createOrder(userId, totalAmount);
        order.applyCoupon(couponId, discountAmount);
        return order;
    }
}