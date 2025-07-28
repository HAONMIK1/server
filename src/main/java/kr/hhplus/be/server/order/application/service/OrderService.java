package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.entity.PaymentEntity;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.repository.PaymentRepository;
import kr.hhplus.be.server.order.presentation.controller.dto.OrderResponse;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final BalanceService balanceService;
    private final CouponService couponService;
    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;


    public OrderResponse.Payment createOrder(Long userId, Long orderId) {
        //  주문 처리
        OrderEntity order = processOrder(orderId);

        //  결제 처리
        PaymentEntity payment = processPayment(userId, order);

        //  주문 완료
        completeOrder(order);

        //  데이터 플랫폼 전송

        return new OrderResponse.Payment(
                payment.getId(),
                payment.getPaidAmount(),
                payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getPaidAt(),
                payment.getRegDt()
        );
    }
    /**
     * 주문 처리
     */
    private OrderEntity processOrder(Long orderId) {
        // 1. 주문 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));

        // 2. 주문 상품 조회 및 재고 확인
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(orderId);
        validateStock(orderItems);

        // 3. 주문 금액 계산
        int totalAmount = calculateTotalAmount(orderItems);
        int discountAmount = calculateDiscountAmount(order, totalAmount);

        // 4. 주문 정보 업데이트
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.calculateFinalAmount();
        orderRepository.save(order);

        return order;
    }

    /**
     * 결제처리
     */
    private PaymentEntity processPayment(Long userId, OrderEntity order) {
        // 1. 잔액 확인
        UserBalanceEntity userBalance = balanceService.getBalance(userId);
        if (userBalance.getAmount() < order.getFinalAmount()) {
            throw new IllegalArgumentException("잔액 부족");
        }

        // 2. 잔액 차감
        balanceService.use(userId, order.getFinalAmount());

        // 3. 결제 내역 생성 및 저장
        PaymentEntity payment = new PaymentEntity(
                order.getId(),
                userId,
                order.getFinalAmount(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                PaymentEntity.PaymentMethod.BALANCE
        );
        PaymentEntity savedPayment = paymentRepository.save(payment);

        // 4. 재고 차감
        updateStock(order.getId());

        // 5. 쿠폰 사용 처리
        if (order.hasCoupon()) {
            couponService.useCoupon(order.getUserCouponId());
        }

        return savedPayment;
    }

    /**
     * 주문 완료
     */
    private void completeOrder(OrderEntity order) {
        order.completeOrder();
        orderRepository.save(order);
    }

    private void validateStock(List<OrderItemEntity> orderItems) {
        for (OrderItemEntity item : orderItems) {
            productService.canPurchase(item.getProductId(), item.getQuantity());
        }
    }

    private int calculateTotalAmount(List<OrderItemEntity> orderItems) {
        return orderItems.stream()
                .mapToInt(OrderItemEntity::getPrice)
                .sum();
    }

    private int calculateDiscountAmount(OrderEntity order, int totalAmount) {
        if (order.hasCoupon()) {
            return couponService.calculateDiscount(order.getUserCouponId(), totalAmount);
        }
        return 0;
    }

    private void updateStock(Long orderId) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItemEntity item : orderItems) {
            ProductEntity product = productService.updateStock(item.getProductId(), item.getQuantity());

            // 재고가 0이면 상품 상태를 SOLD_OUT으로 변경
            if (product.getStockQuantity() == 0) {
                productService.updateProductStatus(item.getProductId(), ProductEntity.ProductStatus.SOLD_OUT);
            }
        }
    }
}

