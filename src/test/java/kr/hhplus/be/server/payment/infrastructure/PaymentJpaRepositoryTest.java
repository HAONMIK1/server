package kr.hhplus.be.server.payment.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PaymentJpaRepositoryTest {
    
    @Autowired
    private PaymentJpaRepository paymentJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private PaymentEntity testPayment;
    private OrderEntity testOrder;
    private UserEntity testUser;
    
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = entityManager.persistAndFlush(testUser);
        
        // 테스트용 상품 생성
        ProductEntity testProduct = new ProductEntity();
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
        testOrder = entityManager.persistAndFlush(testOrder);
        
        // 테스트용 결제 생성
        testPayment = PaymentEntity.createForOrder(testOrder);
        
        entityManager.clear();
    }
    
    @Test
    void 결제정보_저장_성공() {
        // when
        PaymentEntity savedPayment = paymentJpaRepository.save(testPayment);
        
        // then
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getOrderId()).isEqualTo(testOrder.getId());
        assertThat(savedPayment.getPaidAmount()).isEqualTo(testOrder.getFinalAmount());
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentEntity.PaymentMethod.BALANCE);
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentEntity.PaymentStatus.COMPLETED);
    }
    
    @Test
    void 결제정보_ID조회_성공() {
        // given
        PaymentEntity savedPayment = paymentJpaRepository.save(testPayment);
        
        // when
        Optional<PaymentEntity> foundPayment = paymentJpaRepository.findById(savedPayment.getId());
        
        // then
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getOrderId()).isEqualTo(testOrder.getId());
        assertThat(foundPayment.get().getPaidAmount()).isEqualTo(testOrder.getFinalAmount());
    }

    @Test
    void 결제정보_전체조회_성공() {
        // given
        PaymentEntity payment1 = PaymentEntity.createForOrder(testOrder);
        
        // 두 번째 주문과 결제 생성
        ProductEntity testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "테스트 상품2");
        ReflectionTestUtils.setField(testProduct2, "price", 15000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        
        List<OrderItemEntity> orderItems2 = List.of(
                OrderItemEntity.createOrderItem(testProduct2, 1)
        );
        OrderEntity testOrder2 = OrderEntity.createOrder(2L, null, orderItems2);
        testOrder2 = entityManager.persistAndFlush(testOrder2);
        
        PaymentEntity payment2 = PaymentEntity.createForOrder(testOrder2);
        
        paymentJpaRepository.save(payment1);
        paymentJpaRepository.save(payment2);
        
        // when
        List<PaymentEntity> allPayments = paymentJpaRepository.findAll();
        
        // then
        assertThat(allPayments).hasSize(2);
        assertThat(allPayments).extracting("orderId")
                .containsExactlyInAnyOrder(testOrder.getId(), testOrder2.getId());
    }
    
    @Test
    void 결제정보_쿠폰적용주문_저장_성공() {
        // given
        ProductEntity testProduct = new ProductEntity();
        ReflectionTestUtils.setField(testProduct, "name", "쿠폰적용 상품");
        ReflectionTestUtils.setField(testProduct, "price", 20000);
        ReflectionTestUtils.setField(testProduct, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct = entityManager.persistAndFlush(testProduct);
        
        List<OrderItemEntity> orderItems = List.of(
                OrderItemEntity.createOrderItem(testProduct, 1)
        );
        OrderEntity orderWithCoupon = OrderEntity.createOrder(3L, 1L, orderItems);
        orderWithCoupon.applyCoupon(3000); // 3000원 할인
        orderWithCoupon = entityManager.persistAndFlush(orderWithCoupon);
        
        PaymentEntity paymentWithDiscount = PaymentEntity.createForOrder(orderWithCoupon);
        
        // when
        PaymentEntity savedPayment = paymentJpaRepository.save(paymentWithDiscount);
        
        // then
        assertThat(savedPayment.getPaidAmount()).isEqualTo(17000); // 20000 - 3000
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentEntity.PaymentStatus.COMPLETED);
    }
}