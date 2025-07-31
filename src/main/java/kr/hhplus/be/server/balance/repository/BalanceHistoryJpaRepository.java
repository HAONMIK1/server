package kr.hhplus.be.server.balance.repository;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistoryEntity, Long> {
    
}