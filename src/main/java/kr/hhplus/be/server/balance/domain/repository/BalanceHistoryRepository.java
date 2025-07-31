package kr.hhplus.be.server.balance.domain.repository;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;

import java.util.List;

public interface BalanceHistoryRepository {
    BalanceHistoryEntity save(BalanceHistoryEntity balanceHistory);
}