package kr.hhplus.be.server.order.infrastructure;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderItemJpaRepository 테스트")
class OrderItemJpaRepositoryTest {

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private OrderItemEntity testOrderItem;
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
        
        // 테스트용 주문 아이템 생성
        testOrderItem = OrderItemEntity.createOrderItem(testProduct, 3);
        
        entityManager.clear();
    }
    
    @Test
    @DisplayName("주문아이템_저장_성공")
    void 주문아이템_저장_성공() {
        // when
        OrderItemEntity savedOrderItem = orderItemJpaRepository.save(testOrderItem);
        
        // then
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getProductId()).isEqualTo(testProduct.getId());
        assertThat(savedOrderItem.getQuantity()).isEqualTo(3);
        assertThat(savedOrderItem.getPrice()).isEqualTo(30000); // 10000 * 3
    }
    
    @Test
    @DisplayName("주문아이템_ID조회_성공")
    void 주문아이템_ID조회_성공() {
        // given
        OrderItemEntity savedOrderItem = orderItemJpaRepository.save(testOrderItem);
        
        // when
        Optional<OrderItemEntity> foundOrderItem = orderItemJpaRepository.findById(savedOrderItem.getId());
        
        // then
        assertThat(foundOrderItem).isPresent();
        assertThat(foundOrderItem.get().getProductId()).isEqualTo(testProduct.getId());
        assertThat(foundOrderItem.get().getQuantity()).isEqualTo(3);
        assertThat(foundOrderItem.get().getPrice()).isEqualTo(30000);
    }
    
    @Test
    @DisplayName("주문아이템_ID조회_실패_존재하지않음")
    void 주문아이템_ID조회_실패_존재하지않음() {
        // when
        Optional<OrderItemEntity> foundOrderItem = orderItemJpaRepository.findById(999L);
        
        // then
        assertThat(foundOrderItem).isEmpty();
    }
    
    @Test
    @DisplayName("주문아이템_다중상품_저장_성공")
    void 주문아이템_다중상품_저장_성공() {
        // given
        ProductEntity testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "테스트 상품2");
        ReflectionTestUtils.setField(testProduct2, "price", 15000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        
        OrderItemEntity orderItem1 = OrderItemEntity.createOrderItem(testProduct, 2);
        OrderItemEntity orderItem2 = OrderItemEntity.createOrderItem(testProduct2, 1);
        
        // when
        OrderItemEntity savedOrderItem1 = orderItemJpaRepository.save(orderItem1);
        OrderItemEntity savedOrderItem2 = orderItemJpaRepository.save(orderItem2);
        
        // then
        assertThat(savedOrderItem1.getProductId()).isEqualTo(testProduct.getId());
        assertThat(savedOrderItem1.getPrice()).isEqualTo(20000); // 10000 * 2
        
        assertThat(savedOrderItem2.getProductId()).isEqualTo(testProduct2.getId());
        assertThat(savedOrderItem2.getPrice()).isEqualTo(15000); // 15000 * 1
    }
}