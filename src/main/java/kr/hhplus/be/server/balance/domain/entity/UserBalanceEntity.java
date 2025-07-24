package kr.hhplus.be.server.balance.domain.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_balance")
@Getter
@Setter
@NoArgsConstructor
public class UserBalanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance", nullable = false)
    private Integer amount;

    @CreationTimestamp
    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "mdfcn_dt")
    private LocalDateTime mdfcnDt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity userEntity;

    private static final int MIN_CHARGE_AMOUNT = 1000;
    private static final int MAX_CHARGE_AMOUNT = 1000000;
    private static final int MAX_BALANCE_LIMIT = 10000000;

    public UserBalanceEntity(Long userId, Integer amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public void charge(int chargeAmount) {
        validateChargeAmount(chargeAmount);

        int newAmount = this.amount + chargeAmount;
        if (newAmount > MAX_BALANCE_LIMIT) {
            throw new IllegalArgumentException("최대 보유 잔액 초과");
        }

        this.amount = newAmount;
    }

    public void use(int useAmount) {
        if (useAmount <= 0) {
            throw new IllegalArgumentException("사용 금액 양수");
        }

        if (this.amount < useAmount) {
            throw new IllegalArgumentException("잔액 부족");
        }

        this.amount -= useAmount;
    }

    private void validateChargeAmount(int amount) {
        if (amount < MIN_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("최소 충전 금액 미만");
        }
        if (amount > MAX_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("최대 충전 금액 초과");
        }
    }
}
