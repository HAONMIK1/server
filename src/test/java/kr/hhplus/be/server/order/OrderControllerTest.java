package kr.hhplus.be.server.order;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.presentation.controller.OrderController;
import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.order.presentation.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void 주문_처리_성공() throws Exception {
        // given
        Long userId = 1L;
        Long userCouponId = 1L;
        OrderRequest.Create request = new OrderRequest.Create(List.of(new OrderRequest.OrderItem(1L, 2)), userCouponId);

        OrderResponse.Detail response = new OrderResponse.Detail(1L, userId, "PENDING", 20000, 0, 20000, LocalDateTime.now());

        given(orderService.placeOrder(eq(userId), any(OrderRequest.Create.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/orders", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}