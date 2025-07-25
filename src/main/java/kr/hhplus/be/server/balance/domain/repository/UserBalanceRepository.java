package kr.hhplus.be.server.balance.domain.repository;

import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepository extends JpaRepository<UserBalanceEntity, Long> {
    UserBalanceEntity  findByUserId(Long userId);
}