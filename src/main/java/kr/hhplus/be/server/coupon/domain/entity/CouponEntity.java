package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
public class CouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Column(name = "max_amount", nullable = false)
    private Integer maxAmount;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "issued_count", nullable = false)
    private Integer issuedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CouponStatus status;

    @Column(name = "start_dt", nullable = false)
    private LocalDateTime startDt;

    @Column(name = "end_dt", nullable = false)
    private LocalDateTime endDt;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;

    public enum CouponStatus {
        ACTIVE, INACTIVE, EXPIRED
    }
    public void canIssue() {
        LocalDateTime now = LocalDateTime.now();

        // 쿠폰 상태
        if (status != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("쿠폰을 발급할 수 없습니다.");
        }

        // 발급 기간
        if (now.isBefore(startDt) || now.isAfter(endDt)) {
            throw new IllegalArgumentException("쿠폰을 발급할 수 없습니다.");
        }

        // 발급 가능 수량
        if(issuedCount >= quantity){
            throw new IllegalArgumentException("쿠폰을 발급할 수 없습니다.");
        }

    }

    public void increaseIssuedCount() {
        canIssue();
        this.issuedCount++;
    }
    public int calculateDiscount(int totalAmount) {
        if (discountRate == null || discountRate <= 0) {
            return 0;
        }

        // 할인율로 계산
        int discountAmount = (int) (totalAmount * (discountRate / 100.0));

        // 최대 할인 금액 제한
        if (maxAmount != null && discountAmount > maxAmount) {
            return maxAmount;
        }

        return discountAmount;
    }


}