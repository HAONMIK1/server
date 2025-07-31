package kr.hhplus.be.server.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.payment.application.service.PaymentService;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.payment.presntation.controller.PaymentController;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.payment.presntation.dto.PaymentResponse;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void 결제_성공() throws Exception {
        // Given
        Long orderId = 1L;
        Long userId = 1L;
        PaymentRequest.Process request = new PaymentRequest.Process("BALANCE");
        PaymentResponse.PaymentDetail paymentDetail = new PaymentResponse.PaymentDetail(
                100L, orderId, 18000, "BALANCE", PaymentEntity.PaymentStatus.COMPLETED, LocalDateTime.now()
        );
        PaymentResponse.Complete response = new PaymentResponse.Complete(paymentDetail);

        given(paymentService.processPayment(userId, orderId, request))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/users/{userId}/orders/{orderId}/payment", userId, orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment.paymentId").value(100L))
                .andExpect(jsonPath("$.payment.orderId").value(orderId))
                .andExpect(jsonPath("$.payment.paymentStatus").value("COMPLETED"));
    }
} 