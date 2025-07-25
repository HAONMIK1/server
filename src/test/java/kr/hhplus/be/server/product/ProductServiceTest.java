package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.PopularProductRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private PopularProductRepository popularProductRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 상품_목록_조회_성공() {
        // given
        List<ProductEntity> products = Arrays.asList(
                ProductEntity.createProduct(1L, "상품1", 10000, 100),
                ProductEntity.createProduct(2L, "상품2", 20000, 50)
        );
        given(productRepository.findAll()).willReturn(products);

        // when
        List<ProductEntity> result = productService.getProducts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("상품1");
        assertThat(result.get(1).getName()).isEqualTo("상품2");
        verify(productRepository.findAll());
    }

    @Test
    void 상품_상세_조회_성공() {
        // given
        Long productId = 1L;
        ProductEntity product = ProductEntity.createProduct(productId, "테스트 상품", 15000, 75);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductEntity result = productService.getProduct(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        verify(productRepository).findById(productId);
    }

    @Test
    void 인기_상품_조회_성공() {
        // given
        List<PopularProductEntity> popularProducts = Arrays.asList(
                PopularProductEntity.createPopularProduct(1L, 1000, 500),
                PopularProductEntity.createPopularProduct(2L, 800, 300),
                PopularProductEntity.createPopularProduct(3L, 600, 200)
        );
        given(popularProductRepository.findAll()).willReturn(popularProducts);

        // when
        List<PopularProductEntity> result = productService.getPopularProducts();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getViewCount()).isEqualTo(1000);
        assertThat(result.get(0).getSalesCount()).isEqualTo(500);
        assertThat(result.get(1).getViewCount()).isEqualTo(800);
        assertThat(result.get(1).getSalesCount()).isEqualTo(300);
        verify(popularProductRepository).findAll();
    }

}
