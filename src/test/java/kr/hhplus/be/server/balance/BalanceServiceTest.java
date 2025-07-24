package kr.hhplus.be.server.balance;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.balance.presentation.dto.BalanceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BalanceServiceTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void 잔액_충전_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 50000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 100000);
        UserBalanceEntity savedBalance = new UserBalanceEntity(userId, 150000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(savedBalance);
        when(balanceHistoryRepository.save(any(BalanceHistoryEntity.class))).thenReturn(new BalanceHistoryEntity());

        // when
        BalanceResponse result = balanceService.chargeBalance(userId, chargeAmount);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.balance()).isEqualTo(150000);
        verify(userBalanceRepository).save(any(UserBalanceEntity.class));
        verify(balanceHistoryRepository).save(any(BalanceHistoryEntity.class));
    }
    @Test
    void 잔액_최소충전_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 1000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 0);
        UserBalanceEntity savedBalance = new UserBalanceEntity(userId, 1000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(savedBalance);
        when(balanceHistoryRepository.save(any(BalanceHistoryEntity.class))).thenReturn(new BalanceHistoryEntity());

        // when
        BalanceResponse result = balanceService.chargeBalance(userId, chargeAmount);

        // then
        assertThat(result.balance()).isEqualTo(1000);
    }
    @Test
    void 잔액_최대충전_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 1000000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 0);
        UserBalanceEntity savedBalance = new UserBalanceEntity(userId, 1000000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(savedBalance);
        when(balanceHistoryRepository.save(any(BalanceHistoryEntity.class))).thenReturn(new BalanceHistoryEntity());

        // when
        BalanceResponse result = balanceService.chargeBalance(userId, chargeAmount);

        // then
        assertThat(result.balance()).isEqualTo(1000000);
    }
    @Test
    void 잔액_최소충전경계값_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 999;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 0);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);

        // when & then
        assertThatThrownBy(() -> balanceService.chargeBalance(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 충전 금액 미만");
    }
    @Test
    void 잔액_최대충전경계값_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 1000001;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 0);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);

        // when & then
        assertThatThrownBy(() -> balanceService.chargeBalance(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 충전 금액 초과");
    }
    @Test
    void 최대잔고_경계값_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 1000000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 9900000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);

        // when & then
        assertThatThrownBy(() -> balanceService.chargeBalance(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 보유 잔액 초과");
    }

    @Test
    void 잔액_사용_성공() {
        // given
        Long userId = 1L;
        int useAmount = 50000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 100000);
        UserBalanceEntity savedBalance = new UserBalanceEntity(userId, 50000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(savedBalance);
        when(balanceHistoryRepository.save(any(BalanceHistoryEntity.class))).thenReturn(new BalanceHistoryEntity());

        // when
        balanceService.use(userId, useAmount);

        // then
        verify(userBalanceRepository).save(any(UserBalanceEntity.class));
        verify(balanceHistoryRepository).save(any(BalanceHistoryEntity.class));
    }

    @Test
    void 잔액_부족_실패() {
        // given
        Long userId = 1L;
        int useAmount = 50000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 30000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);

        // when & then
        assertThatThrownBy(() -> balanceService.use(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔액 부족");
    }

    @Test
    void 음수_금액_사용_실패() {
        // given
        Long userId = 1L;
        int useAmount = -1000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 50000);

        // when
        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);

        // when & then
        assertThatThrownBy(() -> balanceService.use(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 금액 양수");
    }
    @Test
    void 사용_이력_저장_성공() {
        // given
        Long userId = 1L;
        int useAmount = 5000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 50000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(userBalance);

        // when
        balanceService.use(userId, useAmount);

        // then
        verify(balanceHistoryRepository).save(argThat(history ->
                history.getUserId().equals(userId) &&
                        history.getAmount().equals(useAmount) &&
                        history.getType() == BalanceHistoryEntity.BalanceType.USE
        ));
    }
    @Test
    void 충전_이력_저장_성공() {
        // given
        Long userId = 1L;
        int chargeAmount = 10000;
        UserBalanceEntity userBalance = new UserBalanceEntity(userId, 50000);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(userBalance);
        when(userBalanceRepository.save(any(UserBalanceEntity.class))).thenReturn(userBalance);

        // when
        balanceService.chargeBalance(userId, chargeAmount);

        // then
        verify(balanceHistoryRepository).save(argThat(history ->
                history.getUserId().equals(userId) &&
                        history.getAmount().equals(chargeAmount) &&
                        history.getType() == BalanceHistoryEntity.BalanceType.CHARGE
        ));
    }
}
