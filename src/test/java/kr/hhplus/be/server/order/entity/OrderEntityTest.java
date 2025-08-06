package kr.hhplus.be.server.order.entity;

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
        void 주문_생성_쿠폰_없이_성공() {
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
        void 쿠폰_사용_성공() {
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
        void 주문_완료_상태() {
            // Given
            OrderEntity order = OrderEntity.createOrder(1L, null, List.of());
            
            // When
            order.complete();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED);
        }
        
        @Test
        void 처리된_주문_실패() {
            // Given
            OrderEntity order = OrderEntity.createOrder(1L, null, List.of());
            order.complete();
            
            // When & Then
            assertThatThrownBy(order::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주문 상태가 대기 중이 아닙니다.");
        }

        @Test
        void 주문_유효성_성공() {
            // Given
            Long correctUserId = 1L;
            OrderEntity order = OrderEntity.createOrder(correctUserId, null, List.of());

            // When & Then
            order.validateOrder(correctUserId);
        }

        @Test
        void 주문자_정보_불일치_실패() {
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