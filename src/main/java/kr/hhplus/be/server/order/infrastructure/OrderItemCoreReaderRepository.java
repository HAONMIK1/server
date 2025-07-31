package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderItemCoreReaderRepository implements OrderItemRepository {
    
    private final OrderItemJpaRepository orderItemJpaRepository;
    
    @Override
    public Optional<OrderItemEntity> findById(Long id) {
        return orderItemJpaRepository.findById(id);
    }
    
    @Override
    public OrderItemEntity save(OrderItemEntity orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }
}