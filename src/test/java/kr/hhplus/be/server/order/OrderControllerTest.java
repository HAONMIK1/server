package kr.hhplus.be.server.order;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.presentation.controller.OrderController;
import kr.hhplus.be.server.order.presentation.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.presentation.controller.dto.OrderResponse;
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

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        Long orderId = 1L;
        OrderRequest.Create request = new OrderRequest.Create(orderId, null);
        OrderResponse.Payment response = new OrderResponse.Payment(
                1L,
                10000,
                "BALANCE",
                "COMPLETED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(orderService.createOrder(userId, orderId)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/orders/{orderId}/process", userId, orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.paidAmount").value(response.paidAmount()))
                .andExpect(jsonPath("$.paymentMethod").value(response.paymentMethod()))
                .andExpect(jsonPath("$.paymentStatus").value(response.paymentStatus()))
                .andDo(print());

        verify(orderService).createOrder(userId, orderId);
    }



}