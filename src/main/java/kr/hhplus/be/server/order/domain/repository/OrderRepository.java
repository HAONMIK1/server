package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
