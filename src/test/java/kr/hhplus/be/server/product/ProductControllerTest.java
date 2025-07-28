package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.presentation.controller.ProductController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void 상품_목록_조회() throws Exception {
        // given
        List<ProductEntity> products = Arrays.asList(
                ProductEntity.createProduct(1L, "상품1", 10000, 100),
                ProductEntity.createProduct(2L, "상품2", 20000, 50)
        );
        given(productService.getProducts()).willReturn(products);

        // when & then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("상품1"))
                .andExpect(jsonPath("$[0].price").value(10000))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("상품2"))
                .andExpect(jsonPath("$[1].price").value(20000));

        verify(productService).getProducts();
    }

    @Test
    void 상품_상세_조회() throws Exception {
        // given
        Long productId = 1L;
        ProductEntity product = ProductEntity.createProduct(productId, "테스트 상품", 15000, 75);
        given(productService.getProduct(productId)).willReturn(product);

        // when
        ProductEntity result = productService.getProduct(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        verify(productService).getProduct(productId);
    }

    @Test
    void 인기상품_조회() throws Exception {
        // given
        List<PopularProductEntity> popularProducts = Arrays.asList(
                PopularProductEntity.createPopularProduct(1L, 1000, 500),
                PopularProductEntity.createPopularProduct(2L, 800, 300),
                PopularProductEntity.createPopularProduct(3L, 600, 200),
                PopularProductEntity.createPopularProduct(4L, 400, 100),
                PopularProductEntity.createPopularProduct(5L, 200, 50)
        );
        given(productService.getPopularProducts()).willReturn(popularProducts);

        // when & then
        mockMvc.perform(get("/api/v1/products/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].viewCount").value(1000))
                .andExpect(jsonPath("$[0].salesCount").value(500))
                .andExpect(jsonPath("$[1].viewCount").value(800))
                .andExpect(jsonPath("$[1].salesCount").value(300))
                .andExpect(jsonPath("$[2].viewCount").value(600))
                .andExpect(jsonPath("$[2].salesCount").value(200));

        verify(productService).getPopularProducts();
    }


}
