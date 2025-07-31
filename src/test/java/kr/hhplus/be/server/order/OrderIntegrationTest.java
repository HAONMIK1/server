package kr.hhplus.be.server.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.order.application.service.OrderService;
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
@DisplayName("Order 도메인 통합 테스트")
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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
        ReflectionTestUtils.setField(testProduct1, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct1 = productRepository.save(testProduct1);

        // 테스트용 상품 2
        testProduct2 = new ProductEntity();
        ReflectionTestUtils.setField(testProduct2, "name", "테스트 상품 2");
        ReflectionTestUtils.setField(testProduct2, "price", 20000);
        ReflectionTestUtils.setField(testProduct2, "stockQuantity", 50);
        ReflectionTestUtils.setField(testProduct2, "status", ProductEntity.ProductStatus.AVAILABLE);
        testProduct2 = productRepository.save(testProduct2);
    }











    @Test
    @DisplayName("주문_생성_서비스계층_성공")
    void 주문_생성_서비스계층_성공() {
        // given
        Long userId = 1L;
        List<OrderRequest.OrderItem> orderItems = List.of(
                new OrderRequest.OrderItem(testProduct1.getId(), 2),
                new OrderRequest.OrderItem(testProduct2.getId(), 1)
        );
        OrderRequest.Create request = new OrderRequest.Create(orderItems,1L);

        // when
        var response = orderService.placeOrder(userId, request);

        // then
        assertThat(response.orderId()).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.totalAmount()).isEqualTo(40000); // 10000*2 + 20000*1
        assertThat(response.finalAmount()).isEqualTo(40000);

        // 저장된 주문 확인
        OrderEntity savedOrder = orderRepository.findById(response.orderId()).orElse(null);
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문_완료_서비스계층_성공")
    void 주문_완료_서비스계층_성공() {
        // given - 주문 생성
        List<OrderItemEntity> orderItems = List.of(
                OrderItemEntity.createOrderItem(testProduct1, 1)
        );
        OrderEntity order = OrderEntity.createOrder(1L, null, orderItems);
        OrderEntity savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        // when
        orderService.completeOrder(orderId);

        // then
        OrderEntity completedOrder = orderRepository.findById(orderId).orElse(null);
        assertThat(completedOrder).isNotNull();
        assertThat(completedOrder.getStatus()).isEqualTo(OrderEntity.OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문_단건조회_서비스계층_성공")
    void 주문_단건조회_서비스계층_성공() {
        // given - 주문 생성
        List<OrderItemEntity> orderItems = List.of(
                OrderItemEntity.createOrderItem(testProduct1, 2)
        );
        OrderEntity order = OrderEntity.createOrder(1L, null, orderItems);
        OrderEntity savedOrder = orderRepository.save(order);

        // when
        OrderEntity response = orderService.getOrder(savedOrder.getId());

        // then
        assertThat(response.getId()).isEqualTo(savedOrder.getId());
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualTo(20000); // 10000*2
        assertThat(response.getFinalAmount()).isEqualTo(20000);
    }
}