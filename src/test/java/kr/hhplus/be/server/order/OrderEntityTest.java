package kr.hhplus.be.server.order;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityTest {

    @Test
    void 금액_계산() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 15000);
        order.setDiscountAmount(3000);

        // when
        order.calculateFinalAmount();

        // then
        assertThat(order.getFinalAmount()).isEqualTo(12000);
    }

    @Test
    void 쿠폰_적용() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 10000);

        // when
        order.applyCoupon(1L, 1500);

        // then
        assertThat(order.getUserCouponId()).isEqualTo(1L);
        assertThat(order.getDiscountAmount()).isEqualTo(1500);
        assertThat(order.getFinalAmount()).isEqualTo(8500);
    }

    @Test
    void 주문_완료() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 10000);

        // when
        order.completeOrder();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED);
    }


    @Test
    void 주문_실패() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 10000);

        // when
        order.failOrder();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderEntity.OrderStatus.FAILED);
    }

    @Test
    void 양수금액주문_처리가능여부_true() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 10000);

        // when
        boolean canProcess = order.canProcess();

        // then
        assertThat(canProcess).isTrue();
    }

    @Test
    void 쿠폰_없으면_false() {
        // given
        OrderEntity order = OrderEntity.createOrder(1L, 10000);

        // when
        boolean hasCoupon = order.hasCoupon();

        // then
        assertThat(hasCoupon).isFalse();
    }

    @Test
    void 쿠폰_있으면_true() {
        // given
        OrderEntity order = OrderEntity.createOrderWithCoupon(1L, 10000, 1L, 2000);

        // when
        boolean hasCoupon = order.hasCoupon();

        // then
        assertThat(hasCoupon).isTrue();
    }
}