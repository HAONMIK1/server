package kr.hhplus.be.server.balance.infrastructure;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceJpaRepository extends JpaRepository<UserBalanceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ub FROM UserBalanceEntity ub WHERE ub.userId = :userId ")
    Optional<UserBalanceEntity> findByUserId(@Param("userId") Long userId);
    
}