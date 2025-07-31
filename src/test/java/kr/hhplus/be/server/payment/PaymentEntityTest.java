package kr.hhplus.be.server.payment;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentEntityTest {

    @Test
    void 주문_성공() {
        // given
        Long userId = 1L;
        OrderEntity order = OrderEntity.createOrder(userId, null, Collections.emptyList());
        ReflectionTestUtils.setField(order, "id", 1L);

        // when
        PaymentEntity payment = PaymentEntity.createForOrder(order);

        // then
        assertThat(payment.getOrderId()).isEqualTo(order.getId());
        assertThat(payment.getUserId()).isEqualTo(order.getUserId());
        assertThat(payment.getPaidAmount()).isEqualTo(order.getFinalAmount());
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentEntity.PaymentStatus.COMPLETED);
    }
} 