package kr.hhplus.be.server.order.application.service;

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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderResponse.Detail placeOrder(Long userId, OrderRequest.Create request) {
        // 1. 상품 정보 조회 및 OrderItem 생성
        List<OrderItemEntity> orderItems = request.items().stream()
                .map(item -> {
                    ProductEntity product = productService.getProduct(item.productId());
                    return OrderItemEntity.createOrderItem(product, item.quantity());
                })
                .collect(Collectors.toList());

        // 2. 주문 생성
        OrderEntity newOrder = OrderEntity.createOrder(userId, request.userCouponId(), orderItems);
        OrderEntity savedOrder = orderRepository.save(newOrder);


        return OrderResponse.Detail.from(savedOrder);
    }

    public OrderEntity getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
    }

    public OrderEntity calculateAndFinalizeOrderAmounts(Long orderId) {
        OrderEntity order = getOrder(orderId);
        // 주문 금액을 최종 확정 (쿠폰 할인 등이 이미 적용된 상태)
        return order;
    }

    public void completeOrder(Long orderId) {
        OrderEntity order = getOrder(orderId);
        order.complete();
    }
}

