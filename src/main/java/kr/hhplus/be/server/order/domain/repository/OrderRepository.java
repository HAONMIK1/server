package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<OrderEntity> findById(Long id);
    Optional<OrderEntity> findByIdWithOrderItems(Long id);
    OrderEntity save(OrderEntity order);
}
