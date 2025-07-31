package kr.hhplus.be.server.balance;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.balance.domain.repository.UserRepository;
import kr.hhplus.be.server.balance.presentation.dto.BalanceRequest;
import kr.hhplus.be.server.balance.presentation.dto.BalanceResponse;
import kr.hhplus.be.server.balance.application.service.BalanceService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class BalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private UserBalanceEntity testUserBalance;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트사용자");
        testUser = userRepository.save(testUser);
        
        // 테스트용 사용자 잔액 생성
        testUserBalance = new UserBalanceEntity(testUser.getId(), 10000);
        userBalanceRepository.save(testUserBalance);
    }

    @Test
    void 잔액_조회_API_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/balance", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.amount").value(10000));
    }

    @Test
    void 잔액_충전_API_성공() throws Exception {
        // given
        BalanceRequest.Charge request = new BalanceRequest.Charge(testUser.getId(),5000);

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/balance/charge", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.balance").value(15000)); // 10000 + 5000
    }

    @Test
    void 잔액_충전_API_실패_최대보유잔액초과() throws Exception {
        // given - 현재 잔액을 9,500,000원으로 설정하여 600,000원 충전 시 한도 초과되도록
        testUserBalance.charge(9_490_000); // 10,000 + 9,490,000 = 9,500,000
        userBalanceRepository.save(testUserBalance);
        
        BalanceRequest.Charge request = new BalanceRequest.Charge(testUser.getId(), 600_000); // 충전 후 10,100,000원으로 한도 초과

        // when & then
        mockMvc.perform(post("/api/v1/users/{userId}/balance/charge", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 잔액_충전_서비스계층_성공() {
        // given
        Long userId = testUser.getId();
        int chargeAmount = 5000;

        // when
        BalanceResponse response = balanceService.charge(userId, chargeAmount);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualTo(15000); // 10000 + 5000

        UserBalanceEntity updatedBalance = userBalanceRepository.findByUserId(userId);
        assertThat(updatedBalance.getAmount()).isEqualTo(15000);
    }

    @Test
    void 잔액_충전_서비스계층_실패_최대보유잔액초과() {
        // given - 현재 잔액을 9,500,000원으로 설정하여 600,000원 충전 시 한도 초과되도록
        Long userId = testUser.getId();
        balanceService.charge(userId, 9_490_000); // 10,000 + 9,490,000 = 9,500,000
        int chargeAmount = 600_000; // 충전 후 10,100,000원으로 한도 초과

        // when & then
        assertThatThrownBy(() -> balanceService.charge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 보유 잔액 초과");

        UserBalanceEntity finalBalance = userBalanceRepository.findByUserId(userId);
        assertThat(finalBalance.getAmount()).isEqualTo(9_500_000); // 9,500,000원 유지
    }

    @Test
    void 잔액_사용_서비스계층_성공() {
        // given
        Long userId = testUser.getId();
        int useAmount = 3000;

        // when
        balanceService.use(userId, useAmount);

        // then
        UserBalanceEntity updatedBalance = userBalanceRepository.findByUserId(userId);
        assertThat(updatedBalance.getUserId()).isEqualTo(userId);
        assertThat(updatedBalance.getAmount()).isEqualTo(7000);
    }

    @Test
    void 잔액_사용_서비스계층_실패_잔액부족() {
        // given
        Long userId = testUser.getId();
        int useAmount = 15000; // 잔액 초과

        // when & then
        assertThatThrownBy(() -> balanceService.use(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔액이 부족합니다.");

        UserBalanceEntity finalBalance = userBalanceRepository.findByUserId(userId);
        assertThat(finalBalance.getAmount()).isEqualTo(10000); // 기존 값 유지
    }

    @Test
    void 잔액_조회_서비스계층_성공() {
        // given
        Long userId = testUser.getId();

        // when
        UserBalanceEntity response = balanceService.getBalance(userId);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAmount()).isEqualTo(10000);
    }

}


