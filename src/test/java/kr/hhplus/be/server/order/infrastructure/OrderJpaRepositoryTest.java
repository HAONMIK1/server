package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.UserEntity;
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
class OrderJpaRepositoryTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private OrderEntity testOrder;
    private ProductEntity testProduct;
    private UserEntity testUser;
    
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = entityManager.persistAndFlush(testUser);
        
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
        testOrder = OrderEntity.createOrder(testUser.getId(), null, orderItems);
        
        entityManager.clear();
    }
    
    @Test
    void 주문_저장_성공() {
        // when
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualTo(20000); // 10000 * 2
        assertThat(savedOrder.getFinalAmount()).isEqualTo(20000);
    }
    
    @Test
    void 주문_ID조회_성공() {
        // given
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // when
        Optional<OrderEntity> foundOrder = orderJpaRepository.findById(savedOrder.getId());
        
        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getUserId()).isEqualTo(testUser.getId());
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
    }
    
    @Test
    void 주문_ID조회_실패_존재하지않음() {
        // when
        Optional<OrderEntity> foundOrder = orderJpaRepository.findById(999L);
        
        // then
        assertThat(foundOrder).isEmpty();
    }
    
    @Test
    void 주문_사용자ID조회_성공() {
        // given
        OrderEntity savedOrder = orderJpaRepository.save(testOrder);
        
        // 같은 사용자의 다른 주문 생성
        List<OrderItemEntity> orderItems2 = List.of(
                OrderItemEntity.createOrderItem(testProduct, 1)
        );
        OrderEntity testOrder2 = OrderEntity.createOrder(testUser.getId(), null, orderItems2);
        orderJpaRepository.save(testOrder2);
        
        // when
        List<OrderEntity> orders = orderJpaRepository.findByUserId(testUser.getId());
        
        // then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(order -> order.getUserId().equals(testUser.getId()));
    }
    

}