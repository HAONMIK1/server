package kr.hhplus.be.server.balance.infrastructure;

import kr.hhplus.be.server.balance.domain.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistoryEntity, Long> {
    
}