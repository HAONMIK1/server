package kr.hhplus.be.server.product;

import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.PopularProductEntity;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.presentation.controller.ProductController;
import kr.hhplus.be.server.product.presentation.dto.PopularProductResponse;
import kr.hhplus.be.server.product.presentation.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void 상품목록_조회_성공() throws Exception {
        // given
        ProductEntity product1 = new ProductEntity();
        ReflectionTestUtils.setField(product1, "id", 1L);
        ReflectionTestUtils.setField(product1, "name", "상품1");
        ReflectionTestUtils.setField(product1, "price", 10000);
        ReflectionTestUtils.setField(product1, "totalQuantity", 100);
        ReflectionTestUtils.setField(product1, "stockQuantity", 50);
        ReflectionTestUtils.setField(product1, "status", ProductEntity.ProductStatus.AVAILABLE);

        ProductEntity product2 = new ProductEntity();
        ReflectionTestUtils.setField(product2, "id", 2L);
        ReflectionTestUtils.setField(product2, "name", "상품2");
        ReflectionTestUtils.setField(product2, "price", 20000);
        ReflectionTestUtils.setField(product2, "totalQuantity", 80);
        ReflectionTestUtils.setField(product2, "stockQuantity", 30);
        ReflectionTestUtils.setField(product2, "status", ProductEntity.ProductStatus.AVAILABLE);

        List<ProductEntity> products = Arrays.asList(product1, product2);
        given(productService.getProducts()).willReturn(products);

        // when & then
        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("상품1"))
                .andExpect(jsonPath("$[0].price").value(10000))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("상품2"))
                .andExpect(jsonPath("$[1].price").value(20000));
    }

    @Test
    @DisplayName("상품_상세조회_성공")
    void 상품_상세조회_성공() throws Exception {
        // given
        Long productId = 1L;
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "name", "테스트 상품");
        ReflectionTestUtils.setField(product, "price", 15000);
        ReflectionTestUtils.setField(product, "totalQuantity", 100);
        ReflectionTestUtils.setField(product, "stockQuantity", 25);
        ReflectionTestUtils.setField(product, "status", ProductEntity.ProductStatus.AVAILABLE);

        given(productService.getProduct(productId)).willReturn(product);

        // when & then
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("테스트 상품"))
                .andExpect(jsonPath("$.price").value(15000))
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    @DisplayName("인기상품_조회_성공")
    void 인기상품_조회_성공() throws Exception {
        // given
        PopularProductEntity popularProduct1 = PopularProductEntity.createPopularProduct(1L, 100, 50);
        ReflectionTestUtils.setField(popularProduct1, "id", 1L);
        ReflectionTestUtils.setField(popularProduct1, "regDt", java.time.LocalDateTime.now());
        
        PopularProductEntity popularProduct2 = PopularProductEntity.createPopularProduct(2L, 80, 30);
        ReflectionTestUtils.setField(popularProduct2, "id", 2L);
        ReflectionTestUtils.setField(popularProduct2, "regDt", java.time.LocalDateTime.now());
        
        List<PopularProductEntity> popularProducts = Arrays.asList(popularProduct1, popularProduct2);

        given(productService.getPopularProducts()).willReturn(popularProducts);

        // when & then
        mockMvc.perform(get("/api/v1/products/popular")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].viewCount").value(100))
                .andExpect(jsonPath("$[0].salesCount").value(50))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].viewCount").value(80))
                .andExpect(jsonPath("$[1].salesCount").value(30));
    }

}
