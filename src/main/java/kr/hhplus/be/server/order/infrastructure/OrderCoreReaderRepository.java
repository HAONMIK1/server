package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderCoreReaderRepository implements OrderRepository {
    
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<OrderEntity> findById(Long id) {
        return orderJpaRepository.findById(id);
    }
    
    @Override
    public Optional<OrderEntity> findByIdWithOrderItems(Long id) {
        return orderJpaRepository.findByIdWithOrderItems(id);
    }
    
    @Override
    public OrderEntity save(OrderEntity order) {
        return orderJpaRepository.save(order);
    }

} 