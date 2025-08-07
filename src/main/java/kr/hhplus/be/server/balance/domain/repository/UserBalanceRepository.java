package kr.hhplus.be.server.balance.domain.repository;

import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;

import java.util.Optional;

public interface UserBalanceRepository {
    Optional<UserBalanceEntity> findByUserId(Long userId);
    UserBalanceEntity save(UserBalanceEntity userBalance);
}