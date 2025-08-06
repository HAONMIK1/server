package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PopularProductJpaRepositoryTest {

    @Autowired
    private PopularProductJpaRepository popularProductJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private ProductEntity testProduct1;
    private ProductEntity testProduct2;
    
    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성
        testProduct1 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct1, "name", "인기상품1");
        ReflectionTestUtils.setField(testProduct1, "price", 10000);
        ReflectionTestUtils.setField(testProduct1, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct1, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct1 = entityManager.persistAndFlush(testProduct1);
        
        testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "인기상품2");
        ReflectionTestUtils.setField(testProduct2, "price", 20000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct2 = entityManager.persistAndFlush(testProduct2);
        
        entityManager.clear();
    }
    
    @Test
    void 인기상품_저장_성공() {
        // given
        PopularProductEntity popularProduct = PopularProductEntity.createPopularProduct(
                testProduct1.getId(), 100, 50);
        
        // when
        PopularProductEntity savedProduct = popularProductJpaRepository.save(popularProduct);
        
        // then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductId()).isEqualTo(testProduct1.getId());
        assertThat(savedProduct.getViewCount()).isEqualTo(100);
        assertThat(savedProduct.getSalesCount()).isEqualTo(50);
    }
    
    @Test
    void 인기상품_우선순위조회_성공() {
        // given
        PopularProductEntity popularProduct1 = PopularProductEntity.createPopularProduct(
                testProduct1.getId(), 100, 50);
        PopularProductEntity popularProduct2 = PopularProductEntity.createPopularProduct(
                testProduct2.getId(), 200, 80);
                
        popularProductJpaRepository.save(popularProduct1);
        popularProductJpaRepository.save(popularProduct2);
        
        // when
        List<PopularProductEntity> popularProducts = popularProductJpaRepository.findPopularProductsOrderedByPriority();
        
        // then
        assertThat(popularProducts).hasSize(2);
        // 조회수 우선 정렬이므로 product2가 먼저 와야 함
        assertThat(popularProducts.get(0).getProductId()).isEqualTo(testProduct2.getId());
        assertThat(popularProducts.get(1).getProductId()).isEqualTo(testProduct1.getId());
    }
    
    @Test
    void 인기상품_전체삭제_성공() {
        // given
        PopularProductEntity popularProduct1 = PopularProductEntity.createPopularProduct(
                testProduct1.getId(), 100, 50);
        PopularProductEntity popularProduct2 = PopularProductEntity.createPopularProduct(
                testProduct2.getId(), 200, 80);
                
        popularProductJpaRepository.save(popularProduct1);
        popularProductJpaRepository.save(popularProduct2);
        
        // when
        popularProductJpaRepository.deleteAllPopularProducts();
        popularProductJpaRepository.flush(); // 강제로 쿼리 실행
        
        // then
        List<PopularProductEntity> remainingProducts = popularProductJpaRepository.findPopularProductsOrderedByPriority();
        assertThat(remainingProducts).isEmpty();
    }
    
    @Test
    void 인기상품_통계데이터조회_성공() {
        // given
        PopularProductEntity popularProduct1 = PopularProductEntity.createPopularProduct(
                testProduct1.getId(), 150, 80); // 조회수 150, 판매수 80
        PopularProductEntity popularProduct2 = PopularProductEntity.createPopularProduct(
                testProduct2.getId(), 200, 60); // 조회수 200, 판매수 60

        popularProductJpaRepository.save(popularProduct1);
        popularProductJpaRepository.save(popularProduct2);

        // when
        List<PopularProductEntity> directResults = popularProductJpaRepository.findPopularProductsOrderedByPriority();

        // then
        assertThat(directResults).hasSize(2);
        assertThat(directResults.get(0).getViewCount()).isEqualTo(200);
        assertThat(directResults.get(1).getViewCount()).isEqualTo(150);
    }
}