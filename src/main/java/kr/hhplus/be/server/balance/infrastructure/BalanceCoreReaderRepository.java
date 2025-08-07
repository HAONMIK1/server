package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.domain.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.balance.domain.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BalanceCoreReaderRepository implements UserBalanceRepository, BalanceHistoryRepository {
    
    private final BalanceJpaRepository userBalanceJpaRepository;
    private final BalanceHistoryJpaRepository balanceHistoryJpaRepository;
    
    @Override
    public Optional<UserBalanceEntity> findByUserId(Long userId) {
        return userBalanceJpaRepository.findByUserId(userId);
    }
    
    @Override
    public UserBalanceEntity save(UserBalanceEntity userBalance) {
        return userBalanceJpaRepository.save(userBalance);
    }
    

    @Override
    public BalanceHistoryEntity save(BalanceHistoryEntity balanceHistory) {
        return balanceHistoryJpaRepository.save(balanceHistory);
    }
} 