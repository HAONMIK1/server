package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BalanceHistoryJpaRepositoryTest {
    
    @Autowired
    private BalanceHistoryJpaRepository balanceHistoryJpaRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private BalanceHistoryEntity testBalanceHistory;
    private UserEntity testUser;
    
    @BeforeEach
    void setUp() {
        // 먼저 사용자 생성
        testUser = new UserEntity();
        testUser.setUserName("테스트 사용자");
        testUser = entityManager.persistAndFlush(testUser);
        
        testBalanceHistory = new BalanceHistoryEntity();
        testBalanceHistory.setUserId(testUser.getId());
        testBalanceHistory.setAmount(10000);
        testBalanceHistory.setType(BalanceHistoryEntity.BalanceType.CHARGE);
    }
    
    @Test
    void 잔액이력_저장_성공() {
        // when
        BalanceHistoryEntity savedHistory = balanceHistoryJpaRepository.save(testBalanceHistory);
        
        // then
        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedHistory.getAmount()).isEqualTo(10000);
        assertThat(savedHistory.getType()).isEqualTo(BalanceHistoryEntity.BalanceType.CHARGE);
    }
    
    @Test
    void 잔액이력_사용타입_저장_성공() {
        // given
        BalanceHistoryEntity useHistory = new BalanceHistoryEntity();
        useHistory.setUserId(testUser.getId());
        useHistory.setAmount(5000);
        useHistory.setType(BalanceHistoryEntity.BalanceType.USE);
        
        // when
        BalanceHistoryEntity savedHistory = balanceHistoryJpaRepository.save(useHistory);
        
        // then
        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedHistory.getAmount()).isEqualTo(5000);
        assertThat(savedHistory.getType()).isEqualTo(BalanceHistoryEntity.BalanceType.USE);
    }
    
    @Test
    void 잔액이력_전체조회_성공() {
        // given
        BalanceHistoryEntity chargeHistory = new BalanceHistoryEntity();
        chargeHistory.setUserId(testUser.getId());
        chargeHistory.setAmount(10000);
        chargeHistory.setType(BalanceHistoryEntity.BalanceType.CHARGE);
        
        BalanceHistoryEntity useHistory = new BalanceHistoryEntity();
        useHistory.setUserId(testUser.getId());
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