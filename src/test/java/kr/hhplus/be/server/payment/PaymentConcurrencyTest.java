package kr.hhplus.be.server.payment;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.payment.application.service.PaymentService;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PaymentConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    private Long testUserId;
    private Long testOrderId;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        UserEntity testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        // 테스트용 사용자 잔액 생성
        UserBalanceEntity userBalance = new UserBalanceEntity(testUserId, 100000); // 충분한 잔액
        userBalanceRepository.save(userBalance);

        // 테스트용 상품 생성
        testProduct = new ProductEntity(
                "테스트 상품",
                10000,
                100,
                100,
                ProductEntity.ProductStatus.AVAILABLE
        );
        testProduct = productRepository.save(testProduct);

        // 테스트용 주문 생성
        OrderItemEntity orderItem = OrderItemEntity.builder()
                .productId(testProduct.getId())
                .quantity(1)
                .price(10000)
                .build();

        OrderEntity testOrder = OrderEntity.createOrder(testUserId, null, Arrays.asList(orderItem));
        testOrder = orderRepository.save(testOrder);
        testOrderId = testOrder.getId();

    }

    @Test
    void 결제_동시성() throws InterruptedException {
        // given
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalProcessingTime = new AtomicLong(0);

        // when
        CountDownLatch startLatch = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            long orderId = testOrderId;
            long userId = testUserId;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    long startTime = System.currentTimeMillis();

                    PaymentRequest.Process request = new PaymentRequest.Process("BALANCE");
                    paymentService.processPayment(userId, orderId, request);

                    long endTime = System.currentTimeMillis();
                    totalProcessingTime.addAndGet(endTime - startTime);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("결제 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();
        latch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
    }
}