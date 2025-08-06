
package kr.hhplus.be.server.coupon.entity;

import kr.hhplus.be.server.coupon.domain.entity.CouponEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponEntityTest {

    @Test
    void 발급_가능_쿠폰() {
        // given
        CouponEntity coupon = createValidCoupon();

        // when & then
        coupon.canIssue(); // 예외가 발생하지 않으면 성공
    }

    @Test
    void 비활성_상태_쿠폰은_발급_불가() {
        // given
        CouponEntity coupon = createValidCoupon();
        coupon.setStatus(CouponEntity.CouponStatus.INACTIVE);

        // when & then
        assertThatThrownBy(() -> coupon.canIssue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다.");
    }

    @Test
    void 기간_만료_쿠폰() {
        // given
        CouponEntity coupon = createValidCoupon();
        coupon.setEndDt(LocalDateTime.now().minusDays(1)); // 어제 만료

        // when & then
        assertThatThrownBy(() -> coupon.canIssue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다.");
    }

    @Test
    void 재고_부족_쿠폰() {
        // given
        CouponEntity coupon = createValidCoupon();
        coupon.setQuantity(10);
        coupon.setIssuedCount(10); // 재고 소진

        // when & then
        assertThatThrownBy(() -> coupon.canIssue())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다.");
    }

    @Test
    void 쿠폰_발급_성공() {
        // given
        CouponEntity coupon = createValidCoupon();
        int beforeCount = coupon.getIssuedCount();

        // when
        coupon.increaseIssuedCount();

        // then
        assertThat(coupon.getIssuedCount()).isEqualTo(beforeCount + 1);
    }

    @Test
    void 할인_금액_계산() {
        // given
        CouponEntity coupon = createValidCoupon();
        coupon.setDiscountRate(10); // 10% 할인
        coupon.setMaxAmount(5000); // 최대 5000원 할인
        int totalAmount = 30000;

        // when
        int discount = coupon.calculateDiscount(totalAmount);

        // then
        assertThat(discount).isEqualTo(3000); // 30000 * 0.1 = 3000원
    }

    @Test
    void 최대_할인_금액() {
        // given
        CouponEntity coupon = createValidCoupon();
        coupon.setDiscountRate(20); // 20% 할인
        coupon.setMaxAmount(5000); // 최대 5000원 할인
        int totalAmount = 50000; // 20%면 10000원인데 최대 5000원 제한

        // when
        int discount = coupon.calculateDiscount(totalAmount);

        // then
        assertThat(discount).isEqualTo(5000); // 최대 할인 금액으로 제한
    }

    private CouponEntity createValidCoupon() {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(1L);
        coupon.setCouponName("테스트 쿠폰");
        coupon.setDiscountRate(10);
        coupon.setMaxAmount(10000);
        coupon.setQuantity(100);
        coupon.setIssuedCount(0);
        coupon.setStatus(CouponEntity.CouponStatus.ACTIVE);
        coupon.setStartDt(LocalDateTime.now().minusDays(1));
        coupon.setEndDt(LocalDateTime.now().plusDays(7));
        return coupon;
    }
}
