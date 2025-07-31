package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BalanceHistoryJpaRepository 테스트")
public class BalanceHistoryJpaRepositoryTest {
    
    @Autowired
    private BalanceHistoryJpaRepository balanceHistoryJpaRepository;
    
    private BalanceHistoryEntity testBalanceHistory;
    
    @BeforeEach
    void setUp() {
        testBalanceHistory = new BalanceHistoryEntity();
        testBalanceHistory.setUserId(1L);
        testBalanceHistory.setAmount(10000);
        testBalanceHistory.setType(BalanceHistoryEntity.BalanceType.CHARGE);
    }
    
    @Test
    @DisplayName("잔액이력_저장_성공")
    void 잔액이력_저장_성공() {
        // when
        BalanceHistoryEntity savedHistory = balanceHistoryJpaRepository.save(testBalanceHistory);
        
        // then
        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getUserId()).isEqualTo(1L);
        assertThat(savedHistory.getAmount()).isEqualTo(10000);
        assertThat(savedHistory.getType()).isEqualTo(BalanceHistoryEntity.BalanceType.CHARGE);
    }
    
    @Test
    @DisplayName("잔액이력_사용타입_저장_성공")
    void 잔액이력_사용타입_저장_성공() {
        // given
        BalanceHistoryEntity useHistory = new BalanceHistoryEntity();
        useHistory.setUserId(2L);
        useHistory.setAmount(5000);
        useHistory.setType(BalanceHistoryEntity.BalanceType.USE);
        
        // when
        BalanceHistoryEntity savedHistory = balanceHistoryJpaRepository.save(useHistory);
        
        // then
        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getUserId()).isEqualTo(2L);
        assertThat(savedHistory.getAmount()).isEqualTo(5000);
        assertThat(savedHistory.getType()).isEqualTo(BalanceHistoryEntity.BalanceType.USE);
    }
    
    @Test
    @DisplayName("잔액이력_전체조회_성공")
    void 잔액이력_전체조회_성공() {
        // given
        BalanceHistoryEntity chargeHistory = new BalanceHistoryEntity();
        chargeHistory.setUserId(1L);
        chargeHistory.setAmount(10000);
        chargeHistory.setType(BalanceHistoryEntity.BalanceType.CHARGE);
        
        BalanceHistoryEntity useHistory = new BalanceHistoryEntity();
        useHistory.setUserId(1L);
        useHistory.setAmount(3000);
        useHistory.setType(BalanceHistoryEntity.BalanceType.USE);
        
        balanceHistoryJpaRepository.save(chargeHistory);
        balanceHistoryJpaRepository.save(useHistory);
        
        // when
        var allHistories = balanceHistoryJpaRepository.findAll();
        
        // then
        assertThat(allHistories).hasSize(2);
        assertThat(allHistories).extracting("type")
                .containsExactlyInAnyOrder(
                        BalanceHistoryEntity.BalanceType.CHARGE,
                        BalanceHistoryEntity.BalanceType.USE
                );
    }
}