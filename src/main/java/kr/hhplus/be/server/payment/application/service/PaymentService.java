package kr.hhplus.be.server.payment.application.service;

import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.payment.presntation.dto.PaymentResponse;
import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final BalanceService balanceService;
    private final CouponService couponService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    
    public PaymentResponse.Complete processPayment(Long userId, Long orderId, PaymentRequest.Process request) {
        // 1. 주문 조회 및 검증 (orderItems 포함하여 N+1 문제 방지)
        OrderEntity order = orderService.getOrderWithOrderItems(orderId);
        order.validateForPayment(userId);
        
        // 2. 주문 금액 계산 및 확정
        OrderEntity finalizedOrder = orderService.calculateAndFinalizeOrderAmounts(orderId);
        
        // 3. 재고 차감
        for (OrderItemEntity orderItem : order.getOrderItems()) {
            productService.decreaseStock(orderItem.getProductId(), orderItem.getQuantity());
        }
        
        // 4. 잔액 차감
        balanceService.use(userId, finalizedOrder.getFinalAmount());
        
        // 5. 쿠폰 사용 (쿠폰이 있는 경우)
        if (order.getUserCouponId() != null) {
            couponService.useCoupon(order.getUserCouponId());
        }
        
        // 6. 결제 정보 저장
        PaymentEntity savedPayment = paymentRepository.save(PaymentEntity.from(finalizedOrder, request.paymentMethod()));
        
        // 7. 주문 완료 처리
        finalizedOrder.completeOrder();
        orderRepository.save(finalizedOrder);
        
        return new PaymentResponse.Complete(PaymentResponse.PaymentDetail.from(savedPayment));
    }
}
