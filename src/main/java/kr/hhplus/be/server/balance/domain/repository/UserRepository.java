package kr.hhplus.be.server.balance.domain.repository;

import kr.hhplus.be.server.balance.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
