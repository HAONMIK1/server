package kr.hhplus.be.server.payment.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "original_amount", nullable = false)
    private Integer originalAmount;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Builder
    private PaymentEntity(Long orderId, Long userId, Integer paidAmount, Integer originalAmount, Integer discountAmount, PaymentMethod paymentMethod, PaymentStatus paymentStatus, LocalDateTime paidAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.paidAmount = paidAmount;
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
    }

    public static PaymentEntity createForOrder(OrderEntity order) {
        return PaymentEntity.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .paidAmount(order.getFinalAmount())
                .originalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(PaymentMethod.BALANCE)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();
    }

    public static PaymentEntity from(OrderEntity order, String paymentMethod) {
        return PaymentEntity.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .paidAmount(order.getFinalAmount())
                .originalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                .paymentStatus(PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();
    }

    public static enum PaymentMethod {
        BALANCE,
        CARD,
        CASH
    }

    public static enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // 도메인 로직 메서드들
    public boolean canCancel() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("취소할 수 없는 결제입니다.");
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    public void fail() {
        if (paymentStatus == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 결제는 실패 처리할 수 없습니다.");
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }


}