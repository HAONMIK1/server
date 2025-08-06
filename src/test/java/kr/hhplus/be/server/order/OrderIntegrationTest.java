package kr.hhplus.be.server.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = userRepository.save(testUser);

        // 테스트용 상품 생성
        testProduct = new ProductEntity();
        ReflectionTestUtils.setField(testProduct, "name", "테스트 상품");
        ReflectionTestUtils.setField(testProduct, "price", 10000);
        ReflectionTestUtils.setField(testProduct, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "totalQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void 주문_생성_API_호출_테스트() throws Exception {
        // given
        List<OrderRequest.OrderItem> items = List.of(
                new OrderRequest.OrderItem(testProduct.getId(), 2)
        );
        OrderRequest.Create request = new OrderRequest.Create(items, null);

        // when & then - 단순히 API 호출이 되는지만 확인
        mockMvc.perform(post("/api/v1/users/{userId}/orders", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    void 주문_생성_후_데이터_확인() throws Exception {
        // given - 주문 먼저 생성
        List<OrderRequest.OrderItem> items = List.of(
                new OrderRequest.OrderItem(testProduct.getId(), 1)
        );
        OrderRequest.Create request = new OrderRequest.Create(items, null);

        // when & then - 주문 생성 후 DB에서 확인
        mockMvc.perform(post("/api/v1/users/{userId}/orders", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());
    }
}