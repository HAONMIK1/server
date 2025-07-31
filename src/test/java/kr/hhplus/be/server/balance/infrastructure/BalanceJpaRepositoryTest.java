package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BalanceJpaRepository 테스트")
public class BalanceJpaRepositoryTest {
    
    @Autowired
    private BalanceJpaRepository balanceJpaRepository;
    
    private UserBalanceEntity testUserBalance;
    
    @BeforeEach
    void setUp() {
        testUserBalance = new UserBalanceEntity(1L, 50000);
    }
    
    @Test
    @DisplayName("사용자잔액_저장_성공")
    void 사용자잔액_저장_성공() {
        // when
        UserBalanceEntity savedBalance = balanceJpaRepository.save(testUserBalance);
        
        // then
        assertThat(savedBalance.getId()).isNotNull();
        assertThat(savedBalance.getUserId()).isEqualTo(1L);
        assertThat(savedBalance.getAmount()).isEqualTo(50000);
    }
    
    @Test
    @DisplayName("사용자잔액_사용자ID조회_성공")
    void 사용자잔액_사용자ID조회_성공() {
        // given
        balanceJpaRepository.save(testUserBalance);
        
        // when
        Optional<UserBalanceEntity> foundBalance = balanceJpaRepository.findByUserId(1L);
        
        // then
        assertThat(foundBalance).isPresent();
        assertThat(foundBalance.get().getUserId()).isEqualTo(1L);
        assertThat(foundBalance.get().getAmount()).isEqualTo(50000);
    }
    
    @Test
    @DisplayName("사용자잔액_사용자ID조회_실패_존재하지않음")
    void 사용자잔액_사용자ID조회_실패_존재하지않음() {
        // when
        Optional<UserBalanceEntity> foundBalance = balanceJpaRepository.findByUserId(999L);
        
        // then
        assertThat(foundBalance).isEmpty();
    }
    
    @Test
    @DisplayName("사용자잔액_수정_성공")
    void 사용자잔액_수정_성공() {
        // given
        UserBalanceEntity savedBalance = balanceJpaRepository.save(testUserBalance);
        
        // when
        savedBalance.charge(20000);
        UserBalanceEntity updatedBalance = balanceJpaRepository.save(savedBalance);
        
        // then
        assertThat(updatedBalance.getAmount()).isEqualTo(70000); // 50000 + 20000
    }
}