package kr.hhplus.be.server.order.application.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.lock.DistributedLock;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.order.presentation.dto.OrderResponse;
import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    @DistributedLock(
            key = "'order:' + #userId ",
            waitTime = 10,
            leaseTime = 30,
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public OrderResponse.Detail placeOrder(Long userId, OrderRequest.Create request) {
        List<OrderItemEntity> orderItems = request.items().stream()
                .map(item -> {
                    ProductEntity product = productService.getProduct(item.productId());
                    return OrderItemEntity.createOrderItem(product, item.quantity());
                })
                .collect(Collectors.toList());

        OrderEntity newOrder = OrderEntity.createOrder(userId, request.userCouponId(), orderItems);
        OrderEntity savedOrder = orderRepository.save(newOrder);


        return OrderResponse.Detail.from(savedOrder);
    }

    public OrderEntity getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
    }
    
    public OrderEntity getOrderWithOrderItems(Long orderId) {
        return orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
    }

    public OrderEntity calculateAndFinalizeOrderAmounts(Long orderId) {
        OrderEntity order = getOrder(orderId);
        return order;
    }

    public void completeOrder(Long orderId) {
        OrderEntity order = getOrder(orderId);
        order.complete();
    }
}

