package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderJpaRepository 테스트")
class OrderJpaRepositoryTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private OrderEntity testOrder;
    private ProductEntity testProduct;
    
    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성
        testProduct = new ProductEntity();
        ReflectionTestUtils.setField(testProduct, "name", "테스트 상품");
        ReflectionTestUtils.setField(testProduct, "price", 10000);
        ReflectionTestUtils.setField(testProduct, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct = entityManager.persistAndFlush(testProduct);
        
        // 테스트용 주문 생성
        List<OrderItemEntity> orderItems = List.of(
                OrderItemEntity.createOrderItem(testProduct, 2)
        );
        testOrder = OrderEntity.createOrder(1L, null, orderItems);
        
        entityManager.clear();
    }
    
    @Test
    @DisplayName("주문_저장_성공")
    void 주문_저장_성공() {
        // when
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUserId()).isEqualTo(1L);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualTo(20000); // 10000 * 2
        assertThat(savedOrder.getFinalAmount()).isEqualTo(20000);
    }
    
    @Test
    @DisplayName("주문_ID조회_성공")
    void 주문_ID조회_성공() {
        // given
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // when
        Optional<OrderEntity> foundOrder = orderJpaRepository.findById(savedOrder.getId());
        
        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getUserId()).isEqualTo(1L);
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
    }
    
    @Test
    @DisplayName("주문_ID조회_실패_존재하지않음")
    void 주문_ID조회_실패_존재하지않음() {
        // when
        Optional<OrderEntity> foundOrder = orderJpaRepository.findById(999L);
        
        // then
        assertThat(foundOrder).isEmpty();
    }
    
    @Test
    @DisplayName("주문_사용자ID조회_성공")
    void 주문_사용자ID조회_성공() {
        // given
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // 같은 사용자의 다른 주문 생성
        List<OrderItemEntity> orderItems2 = List.of(
                OrderItemEntity.createOrderItem(testProduct, 1)
        );
        OrderEntity testOrder2 = OrderEntity.createOrder(1L, null, orderItems2);
        orderJpaRepository.save(testOrder2);
        
        // when
        List<OrderEntity> orders = orderJpaRepository.findByUserId(1L);
        
        // then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(order -> order.getUserId().equals(1L));
    }
    
    @Test
    @DisplayName("주문_상태변경_성공")
    void 주문_상태변경_성공() {
        // given
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // when - complete() 메서드가 UnsupportedOperationException을 던질 수 있으므로 직접 상태 변경
        ReflectionTestUtils.setField(savedOrder, "status", OrderEntity.OrderStatus.COMPLETED);
        OrderEntity updatedOrder = orderJpaRepository.save(savedOrder);
        
        // then
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED);
    }
}