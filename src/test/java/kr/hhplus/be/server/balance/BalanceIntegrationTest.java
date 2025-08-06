package kr.hhplus.be.server.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.balance.presentation.dto.BalanceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class BalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = userRepository.save(testUser);

        // 초기 잔액 설정
        UserBalanceEntity userBalance = new UserBalanceEntity(testUser.getId(), 50000);
        userBalanceRepository.save(userBalance);
    }

    @Test
    void 잔액_조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/balance", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void 잔액_충전_API_성공() throws Exception {
        // given
        BalanceRequest.Charge request = new BalanceRequest.Charge(testUser.getId(), 30000);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/balance/charge", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.balance").exists());
    }
}