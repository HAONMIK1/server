package kr.hhplus.be.server.order;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderEntityTest {

    private ProductEntity createProduct(Long id, int price) {
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "price", price);
        return product;
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {
        @Test
        @DisplayName("성공 - 쿠폰 없이")
        void create_order_without_coupon() {
            // given
            Long userId = 1L;
            ProductEntity product1 = createProduct(1L, 10000);
            ProductEntity product2 = createProduct(2L, 5000);

            List<OrderItemEntity> items = List.of(
                    OrderItemEntity.createOrderItem(product1, 2), // 20000
                    OrderItemEntity.createOrderItem(product2, 1)   // 5000
            );

            // when
            OrderEntity order = OrderEntity.createOrder(userId, null, items);

            // then
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getTotalAmount()).isEqualTo(25000);
            assertThat(order.getFinalAmount()).isEqualTo(25000);
            assertThat(order.getDiscountAmount()).isZero();
            assertThat(order.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
            assertThat(order.hasCoupon()).isFalse();
        }

        @Test
        @DisplayName("성공 - 쿠폰 사용")
        void create_order_with_coupon() {
            // given
            Long userId = 1L;
            Long userCouponId = 10L;
            ProductEntity product = createProduct(1L, 10000);

            List<OrderItemEntity> items = List.of(OrderItemEntity.createOrderItem(product, 1));

            // when
            OrderEntity order = OrderEntity.createOrder(userId, userCouponId, items);

            // then
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getUserCouponId()).isEqualTo(userCouponId);
            assertThat(order.getTotalAmount()).isEqualTo(10000);
            assertThat(order.hasCoupon()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 변경 및 검증 테스트")
    class ChangeStatusTest {

        @Test
        @DisplayName("성공 - 주문을 완료 상태로 변경한다.")
        void complete_order_success() {
            // Given
            OrderEntity order = OrderEntity.createOrder(1L, null, List.of());
            
            // When
            order.complete();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED);
        }
        
        @Test
        @DisplayName("실패 - 이미 처리된 주문을 완료할 수 없다.")
        void complete_order_fail_already_processed() {
            // Given
            OrderEntity order = OrderEntity.createOrder(1L, null, List.of());
            order.complete();
            
            // When & Then
            assertThatThrownBy(order::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주문 상태가 대기 중이 아닙니다.");
        }

        @Test
        @DisplayName("성공 - 주문 유효성을 검증한다.")
        void validate_order_success() {
            // Given
            Long correctUserId = 1L;
            OrderEntity order = OrderEntity.createOrder(correctUserId, null, List.of());

            // When & Then (should not throw exception)
            order.validateOrder(correctUserId);
        }

        @Test
        @DisplayName("실패 - 주문자 정보가 일치하지 않는다.")
        void validate_order_fail_wrong_user() {
            // Given
            Long correctUserId = 1L;
            Long wrongUserId = 2L;
            OrderEntity order = OrderEntity.createOrder(correctUserId, null, List.of());
            
            // When & Then
            assertThatThrownBy(() -> order.validateOrder(wrongUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문자 정보가 일치하지 않습니다.");
        }
    }
}