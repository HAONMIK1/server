package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@Getter
@Setter
@NoArgsConstructor
public class UserCouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserCouponStatus status;

    @CreationTimestamp
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;


    public enum UserCouponStatus {
        UNUSED, USED, EXPIRED
    }

    public static UserCouponEntity create(Long userId, Long couponId) {
        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.userId = userId;
        userCoupon.couponId = couponId;
        userCoupon.status = UserCouponStatus.UNUSED;
        return userCoupon;
    }

    public void use() {
        if (status != UserCouponStatus.UNUSED) {
            throw new IllegalStateException("사용된 쿠폰");
        }
        this.status = UserCouponStatus.USED;
    }

    public boolean canUse() {
        return status == UserCouponStatus.UNUSED;
    }
}