package kr.hhplus.be.server.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Product 도메인 통합 테스트")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity testProduct1;
    private ProductEntity testProduct2;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 1
        testProduct1 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct1, "name", "테스트 상품 1");
        ReflectionTestUtils.setField(testProduct1, "price", 10000);
        ReflectionTestUtils.setField(testProduct1, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct1, "totalQuantity", 100);
        ReflectionTestUtils.setField(testProduct1, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct1 = productRepository.save(testProduct1);

        // 테스트용 상품 2
        testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "테스트 상품 2");
        ReflectionTestUtils.setField(testProduct2, "price", 20000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "totalQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    @DisplayName("상품_전체조회_API_성공")
    void 상품_전체조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[0].stockQuantity").exists());
    }

    @Test
    @DisplayName("상품_단건조회_API_성공")
    void 상품_단건조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/products/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.name").value("테스트 상품 1"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.stockQuantity").value(100));
    }



    @Test
    @DisplayName("인기상품_조회_API_성공")
    void 인기상품_조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/products/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("인기상품_업데이트_API_성공")
    void 인기상품_업데이트_API_성공() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/products/popular/update"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체_상품조회_서비스계층_테스트")
    void 전체_상품조회_서비스계층_테스트() {
        // when
        List<ProductEntity> products = productRepository.findAll();

        // then
        assertThat(products).hasSize(2);
        assertThat(products).extracting("name")
                .containsExactlyInAnyOrder("테스트 상품 1", "테스트 상품 2");
    }
}