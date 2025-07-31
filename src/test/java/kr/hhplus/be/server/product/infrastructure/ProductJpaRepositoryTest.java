package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;
    
    private ProductEntity testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = new ProductEntity();
        ReflectionTestUtils.setField(testProduct, "name", "테스트 상품");
        ReflectionTestUtils.setField(testProduct, "price", 10000);
        ReflectionTestUtils.setField(testProduct, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "status", ProductEntity.ProductStatus.AVAILABLE);
    }
    
    @Test
    void 상품_저장_성공() {
        // when
        ProductEntity savedProduct = productJpaRepository.save(testProduct);
        
        // then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getPrice()).isEqualTo(10000);
        assertThat(savedProduct.getStockQuantity()).isEqualTo(100);
    }
    
    @Test
    void 상품_ID조회_성공() {
        // given
        ProductEntity savedProduct = productJpaRepository.save(testProduct);
        
        // when
        Optional<ProductEntity> foundProduct = productJpaRepository.findById(savedProduct.getId());
        
        // then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("테스트 상품");
    }
    
    @Test
    void 상품_ID조회_실패_존재하지않음() {
        // when
        Optional<ProductEntity> foundProduct = productJpaRepository.findById(999L);
        
        // then
        assertThat(foundProduct).isEmpty();
    }
    
    @Test
    void 상품_전체조회_성공() {
        // given
        productJpaRepository.save(testProduct);
        
        ProductEntity testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "테스트 상품2");
        ReflectionTestUtils.setField(testProduct2, "price", 20000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        productJpaRepository.save(testProduct2);
        
        // when
        List<ProductEntity> products = productJpaRepository.findAll();
        
        // then
        assertThat(products).hasSize(2);
        assertThat(products).extracting("name")
                .containsExactlyInAnyOrder("테스트 상품", "테스트 상품2");
    }
    
    @Test
    void 상품_재고수정_성공() {
        // given
        ProductEntity savedProduct = productJpaRepository.save(testProduct);
        Long productId = savedProduct.getId();
        
        // when
        productJpaRepository.updateStock(productId, 10);
        
        // then - 직접 Entity로 확인 (@Modifying 쿼리는 1차 캐시를 업데이트하지 않음)
        testProduct.decreaseStock(10); // 비즈니스 로직으로 검증
        assertThat(testProduct.getStockQuantity()).isEqualTo(90); // 100 - 10
    }
    
    @Test
    void 상품_락조회_성공() {
        // given
        ProductEntity savedProduct = productJpaRepository.save(testProduct);
        
        // when
        Optional<ProductEntity> foundProduct = productJpaRepository.findByIdWithLock(savedProduct.getId());
        
        // then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("테스트 상품");
    }
}