package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
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
    @DisplayName("상품 목록 조회 API - 성공")
    void getProducts_Success() throws Exception {
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

    @DisplayName("존재하는 상품을 반환한다")
    void it_returns_existing_product() {
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
}
