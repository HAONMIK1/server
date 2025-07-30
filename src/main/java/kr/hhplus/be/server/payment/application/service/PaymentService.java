package kr.hhplus.be.server.payment.application.service;

import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.payment.presntation.dto.PaymentResponse;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceService balanceService;
    private final CouponService couponService;
    private final ProductService productService;
    private final OrderService orderService;

    public PaymentResponse.Complete processPayment(Long userId, Long orderId, PaymentRequest.Process request) {
        // 1. 주문 조회 및 검증
        OrderEntity order = orderService.getOrder(orderId);
        order.validateForPayment(userId);

        // 2. 최종 결제 금액 확정
        OrderEntity finalizedOrder = orderService.calculateAndFinalizeOrderAmounts(orderId);

        // 3. 재고 차감, 잔액 사용, 쿠폰 사용
        processPaymentCore(userId, finalizedOrder);

        // 4. 주문 및 결제 상태 업데이트 및 저장
        finalizedOrder.completeOrder();
        orderRepository.save(finalizedOrder);
        PaymentEntity savedPayment = paymentRepository.save(PaymentEntity.from(finalizedOrder, request.paymentMethod()));

        // 5. 최종 결제 응답 생성
        PaymentResponse.PaymentDetail paymentDetail = PaymentResponse.PaymentDetail.from(savedPayment);
        return new PaymentResponse.Complete(paymentDetail);
    }
    
    private void processPaymentCore(Long userId, OrderEntity order) {
        // 재고 차감
        for (OrderItemEntity orderItem : order.getOrderItems()) {
            productService.decreaseStock(orderItem.getProductId(), orderItem.getQuantity());
        }
        // 사용자 잔액 차감
        balanceService.use(userId, order.getFinalAmount());
        // 쿠폰 사용 처리
        if (order.hasCoupon()) {
            couponService.useCoupon(order.getUserCouponId());
        }
    }
}
