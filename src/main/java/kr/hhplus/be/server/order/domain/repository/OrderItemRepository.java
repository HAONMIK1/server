package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;

import java.util.Optional;

public interface OrderItemRepository {
    Optional<OrderItemEntity> findById(Long id);
    OrderItemEntity save(OrderItemEntity orderItem);
}
