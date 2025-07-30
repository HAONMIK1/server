package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_result")
@Getter
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
    private Integer finalAmount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;

    public static OrderEntity createOrder(Long userId, Long userCouponId, List<OrderItemEntity> orderItems) {
        OrderEntity order = new OrderEntity();
        order.userId = userId;
        order.userCouponId = userCouponId;
        order.orderItems = orderItems;
        order.orderTime = LocalDateTime.now();
        order.status = OrderStatus.PENDING;
        order.calculateTotalAmount();
        return order;
    }

    private void calculateTotalAmount() {
        this.totalAmount = this.orderItems.stream()
                .mapToInt(OrderItemEntity::getPrice)
                .sum();
        this.finalAmount = this.totalAmount;
    }

    public void applyCoupon(int discountAmount) {
        if (this.userCouponId == null) {
            throw new IllegalStateException("적용할 쿠폰이 없습니다.");
        }
        if (discountAmount > 0) {
            this.discountAmount = discountAmount;
            this.finalAmount = this.totalAmount - discountAmount;
        }
    }

    public void complete() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("주문 상태가 대기 중이 아닙니다.");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void validateOrder(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new IllegalArgumentException("주문자 정보가 일치하지 않습니다.");
        }
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
    }

    public void validateForPayment(Long userId) {
        validateOrder(userId);
    }

    public void completeOrder() {
        complete();
    }

    public boolean hasCoupon() {
        return this.userCouponId != null;
    }

    public Integer getFinalAmount() {
        return this.finalAmount;
    }

    public enum OrderStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}