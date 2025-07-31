
package kr.hhplus.be.server.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.presentation.controller.BalanceController;
import kr.hhplus.be.server.balance.presentation.dto.BalanceRequest;
import kr.hhplus.be.server.balance.presentation.dto.BalanceResponse;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BalanceControllerTest {

    @InjectMocks
    private BalanceController balanceController;

    @Mock
    private BalanceService balanceService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(balanceController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void 잔액_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 10000);
        given(balanceService.getBalance(userId)).willReturn(userBalance);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/balance", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.amount").value(10000));

        verify(balanceService).getBalance(userId);
    }

    @Test
    void 잔액_충전_성공() throws Exception {
        // given
        Long userId = 1L;
        Integer chargeAmount = 5000;
        BalanceRequest.Charge request = new BalanceRequest.Charge(userId, chargeAmount);
        BalanceResponse response = new BalanceResponse(userId, 15000, LocalDateTime.now());

        given(balanceService.charge(eq(userId), eq(chargeAmount))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/balance/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(15000));

        verify(balanceService).charge(userId, chargeAmount);
    }
}
