package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ProductConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void 상품_재고_동시성() throws InterruptedException {
        // given
        // 초기 재고가 3개인 상품 생성
        ProductEntity initialProduct = new ProductEntity("테스트 상품", 10000, 10, 3, ProductEntity.ProductStatus.AVAILABLE);

        ProductEntity savedProduct = productRepository.save(initialProduct);
        final Long productId = savedProduct.getId();

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 재고 부족 예외는 정상적인 동작
                    System.out.println("Exception: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // then
        ProductEntity finalProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        System.out.println("최종 재고: " + finalProduct.getStockQuantity());
        System.out.println("성공 카운트: " + successCount.get());
        System.out.println("실패 카운트: " + failCount.get());

        // 초기 재고 3개에서 성공한 횟수만큼 차감되어야 함
        assertThat(finalProduct.getStockQuantity()).isEqualTo(0); // 3개 모두 차감되어야 함
        assertThat(successCount.get()).isEqualTo(3); // 3개만 성공해야 함
        assertThat(failCount.get()).isEqualTo(2); // 2개는 실패해야 함
    }


}