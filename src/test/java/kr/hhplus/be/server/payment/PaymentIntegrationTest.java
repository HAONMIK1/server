package kr.hhplus.be.server.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UserEntity testUser;
    private ProductEntity testProduct;
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = userRepository.save(testUser);

        // 사용자 잔액 설정
        UserBalanceEntity userBalance = new UserBalanceEntity(testUser.getId(), 100000);
        userBalanceRepository.save(userBalance);

        // 테스트용 상품 생성
        testProduct = new ProductEntity();
        ReflectionTestUtils.setField(testProduct, "name", "테스트 상품");
        ReflectionTestUtils.setField(testProduct, "price", 30000);
        ReflectionTestUtils.setField(testProduct, "stockQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "totalQuantity", 100);
        ReflectionTestUtils.setField(testProduct, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct = productRepository.save(testProduct);

        // 테스트용 주문 생성 (모든 필수 필드 설정)
        testOrder = new OrderEntity();
        ReflectionTestUtils.setField(testOrder, "userId", testUser.getId());
        ReflectionTestUtils.setField(testOrder, "totalAmount", 60000);
        ReflectionTestUtils.setField(testOrder, "discountAmount", 0);
        ReflectionTestUtils.setField(testOrder, "finalAmount", 60000);
        ReflectionTestUtils.setField(testOrder, "status", OrderEntity.OrderStatus.PENDING);
        ReflectionTestUtils.setField(testOrder, "orderTime", LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void 결제_처리_API_호출_테스트() throws Exception {
        // given
        PaymentRequest.Process request = new PaymentRequest.Process("BALANCE");

        // when & then - 단순히 API 호출이 되는지만 확인
        mockMvc.perform(post("/api/v1/users/{userId}/orders/{orderId}/payment", 
                        testUser.getId(), testOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment.paymentId").exists());
    }
}