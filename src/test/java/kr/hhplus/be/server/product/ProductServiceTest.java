package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductSalesCountEntity;
import kr.hhplus.be.server.product.domain.entity.ProductViewCountEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductSalesCountRepository;
import kr.hhplus.be.server.product.domain.repository.ProductViewCountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PopularProductRepository popularProductRepository;

    @Mock
    private ProductViewCountRepository productViewCountRepository;

    @Mock
    private ProductSalesCountRepository productSalesCountRepository;

    @Test
    @DisplayName("상품목록_조회_성공")
    void 상품목록_조회_성공() {
        // given
        ProductEntity product1 = new ProductEntity();
        ProductEntity product2 = new ProductEntity();
        List<ProductEntity> products = Arrays.asList(product1, product2);

        given(productRepository.findAll()).willReturn(products);

        // when
        List<ProductEntity> result = productService.getProducts();

        // then
        assertThat(result).hasSize(2);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("상품_조회_성공")
    void 상품_조회_성공() {
        // given
        Long productId = 1L;
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "name", "테스트 상품");
        ReflectionTestUtils.setField(product, "price", 10000);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productViewCountRepository.findByProductId(productId)).willReturn(Optional.empty());
        given(productViewCountRepository.save(any(ProductViewCountEntity.class))).willReturn(new ProductViewCountEntity());

        // when
        ProductEntity result = productService.getProduct(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        verify(productRepository).findById(productId);
        verify(productViewCountRepository).findByProductId(productId);
        verify(productViewCountRepository).save(any(ProductViewCountEntity.class));
    }

    @Test
    @DisplayName("상품_조회_존재하지않음_예외")
    void 상품_조회_존재하지않음_예외() {
        // given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("조회수_증가_신규상품")
    void 조회수_증가_신규상품() {
        // given
        Long productId = 1L;
        given(productViewCountRepository.findByProductId(productId)).willReturn(Optional.empty());
        given(productViewCountRepository.save(any(ProductViewCountEntity.class))).willReturn(new ProductViewCountEntity());

        // when
        productService.increaseViewCount(productId);

        // then
        verify(productViewCountRepository).findByProductId(productId);
        verify(productViewCountRepository).save(any(ProductViewCountEntity.class));
    }

    @Test
    @DisplayName("조회수_증가_기존상품")
    void 조회수_증가_기존상품() {
        // given
        Long productId = 1L;
        ProductViewCountEntity viewCount = new ProductViewCountEntity();
        viewCount.setProductId(productId);
        viewCount.setViewCount(50);

        given(productViewCountRepository.findByProductId(productId)).willReturn(Optional.of(viewCount));
        given(productViewCountRepository.save(any(ProductViewCountEntity.class))).willReturn(viewCount);

        // when
        productService.increaseViewCount(productId);

        // then
        verify(productViewCountRepository).findByProductId(productId);
        verify(productViewCountRepository).save(any(ProductViewCountEntity.class));
    }

    @Test
    @DisplayName("재고_확인_성공")
    void 재고_확인_성공() {
        // given
        Long productId = 1L;
        int quantity = 5;
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        productService.checkStock(productId, quantity);

        // then
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("재고_확인_부족_예외")
    void 재고_확인_부족_예외() {
        // given
        Long productId = 1L;
        int quantity = 10;
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 5);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.checkStock(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 재고가 부족합니다.");
    }

    @Test
    @DisplayName("재고_차감_성공")
    void 재고_차감_성공() {
        // given
        Long productId = 1L;
        int quantity = 3;
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productSalesCountRepository.findByProductId(productId)).willReturn(Optional.empty());
        given(productSalesCountRepository.save(any(ProductSalesCountEntity.class))).willReturn(new ProductSalesCountEntity());

        // when
        productService.decreaseStock(productId, quantity);

        // then
        verify(productRepository).findById(productId);
        verify(productSalesCountRepository).findByProductId(productId);
        verify(productSalesCountRepository).save(any(ProductSalesCountEntity.class));
    }

    @Test
    @DisplayName("재고_차감_상품없음_예외")
    void 재고_차감_상품없음_예외() {
        // given
        Long productId = 999L;
        int quantity = 3;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.decreaseStock(productId, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("판매수_증가_신규상품")
    void 판매수_증가_신규상품() {
        // given
        Long productId = 1L;
        given(productSalesCountRepository.findByProductId(productId)).willReturn(Optional.empty());
        given(productSalesCountRepository.save(any(ProductSalesCountEntity.class))).willReturn(new ProductSalesCountEntity());

        // when
        productService.increaseSalesCount(productId);

        // then
        verify(productSalesCountRepository).findByProductId(productId);
        verify(productSalesCountRepository).save(any(ProductSalesCountEntity.class));
    }

    @Test
    @DisplayName("판매수_증가_기존상품")
    void 판매수_증가_기존상품() {
        // given
        Long productId = 1L;
        ProductSalesCountEntity salesCount = new ProductSalesCountEntity();
        salesCount.setProductId(productId);
        salesCount.setSalesCount(25);

        given(productSalesCountRepository.findByProductId(productId)).willReturn(Optional.of(salesCount));
        given(productSalesCountRepository.save(any(ProductSalesCountEntity.class))).willReturn(salesCount);

        // when
        productService.increaseSalesCount(productId);

        // then
        verify(productSalesCountRepository).findByProductId(productId);
        verify(productSalesCountRepository).save(any(ProductSalesCountEntity.class));
    }

    @Test
    @DisplayName("인기상품_조회_성공")
    void 인기상품_조회_성공() {
        // given
        PopularProductEntity popularProduct1 = PopularProductEntity.createPopularProduct(1L, 100, 50);
        PopularProductEntity popularProduct2 = PopularProductEntity.createPopularProduct(2L, 80, 30);
        List<PopularProductEntity> popularProducts = Arrays.asList(popularProduct1, popularProduct2);

        given(popularProductRepository.findPopularProductsOrderedByPriority()).willReturn(popularProducts);

        // when
        List<PopularProductEntity> result = productService.getPopularProducts();

        // then
        assertThat(result).hasSize(2);
        verify(popularProductRepository).findPopularProductsOrderedByPriority();
    }

    @Test
    @DisplayName("인기상품_업데이트_성공")
    void 인기상품_업데이트_성공() {
        // given
        Object[] productData1 = {1L, 50, 100, java.time.LocalDateTime.now()};
        Object[] productData2 = {2L, 30, 80, java.time.LocalDateTime.now()};
        List<Object[]> popularProductsData = Arrays.asList(productData1, productData2);

        given(popularProductRepository.findPopularProductsData()).willReturn(popularProductsData);
        given(popularProductRepository.save(any(PopularProductEntity.class))).willReturn(PopularProductEntity.createPopularProduct(1L, 100, 50));

        // when
        productService.updatePopularProducts();

        // then
        verify(popularProductRepository).deleteAllPopularProducts();
        verify(popularProductRepository).findPopularProductsData();
        verify(popularProductRepository, org.mockito.Mockito.times(2)).save(any(PopularProductEntity.class));
    }
}
