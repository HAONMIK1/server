package kr.hhplus.be.server.balance.domain.repository;

import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;

public interface UserBalanceRepository {
    UserBalanceEntity findByUserId(Long userId);
    UserBalanceEntity save(UserBalanceEntity userBalance);
}