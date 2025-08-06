package kr.hhplus.be.server.balance.application.service;

import kr.hhplus.be.server.balance.presentation.dto.BalanceResponse;
import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BalanceService {
    private final UserBalanceRepository userBalanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    public BalanceResponse charge(Long userId, int amount) {//포인트 충전
        UserBalanceEntity userBalance = getBalance(userId);
        userBalance.charge(amount);
        UserBalanceEntity savedBalance = userBalanceRepository.save(userBalance);
        saveChargeHistory(userId, amount);
        return BalanceResponse.from(savedBalance);
    }

    public void use(Long userId, Integer amount) {//포인트 차감
        UserBalanceEntity userBalance = getBalance(userId);
        userBalance.use(amount);
        userBalanceRepository.save(userBalance);
        saveUseHistory(userId, amount);
    }

    public UserBalanceEntity getBalance(Long userId) {//포인트 조회
        return userBalanceRepository.findByUserId(userId);
    }

    private void saveChargeHistory(Long userId, int amount) {//포인트 충전내역 저장
        BalanceHistoryEntity history = new BalanceHistoryEntity();
        history.setUserId(userId);
        history.setAmount(amount);
        history.setType(BalanceHistoryEntity.BalanceType.CHARGE);
        balanceHistoryRepository.save(history);
    }

    private void saveUseHistory(Long userId, int amount) {//포인트 사용내역 저장
        BalanceHistoryEntity history = new BalanceHistoryEntity();
        history.setUserId(userId);
        history.setAmount(amount);
        history.setType(BalanceHistoryEntity.BalanceType.USE);
        balanceHistoryRepository.save(history);
    }
}
