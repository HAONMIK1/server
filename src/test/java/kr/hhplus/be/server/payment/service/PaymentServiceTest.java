package kr.hhplus.be.server.payment.service;

import kr.hhplus.be.server.balance.application.service.BalanceService;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.entity.OrderItemEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.payment.application.service.PaymentService;
import kr.hhplus.be.server.payment.domain.entity.PaymentEntity;
import kr.hhplus.be.server.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.payment.presntation.dto.PaymentResponse;
import kr.hhplus.be.server.product.application.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private CouponService couponService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void 결제_성공_쿠폰_미사용() {
        // given
        Long orderId = 1L;
        Long userId = 1L;

        // OrderItemEntity 생성
        OrderItemEntity orderItem1 = OrderItemEntity.builder()
                .productId(1L)
                .quantity(2)
                .price(10000)
                .build();

        OrderItemEntity orderItem2 = OrderItemEntity.builder()
                .productId(2L)
                .quantity(1)
                .price(5000)
                .build();

        // OrderEntity 생성 (쿠폰 미사용)
        OrderEntity order = OrderEntity.createOrder(userId, null, Arrays.asList(orderItem1, orderItem2));
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "finalAmount", 15000);
        ReflectionTestUtils.setField(order, "totalAmount", 15000);

        // PaymentEntity 생성
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(orderId)
                .userId(userId)
                .paidAmount(15000)
                .originalAmount(15000)
                .discountAmount(0)
                .paymentMethod(PaymentEntity.PaymentMethod.BALANCE)
                .paymentStatus(PaymentEntity.PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentRequest.Process request = new PaymentRequest.Process("BALANCE");

        // Mock 설정
        given(orderService.getOrderWithOrderItems(orderId)).willReturn(order);
        given(orderService.calculateAndFinalizeOrderAmounts(orderId)).willReturn(order);
        given(orderRepository.save(any(OrderEntity.class))).willReturn(order);
        given(paymentRepository.save(any(PaymentEntity.class))).willReturn(payment);

        // ProductService, BalanceService Mock 설정
        doNothing().when(productService).decreaseStock(anyLong(), anyInt());
        doNothing().when(balanceService).use(any(Long.class), any(Integer.class));

        // when
        PaymentResponse.Complete result = paymentService.processPayment(userId, orderId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.payment()).isNotNull();

        // 호출 검증
        verify(orderService).getOrderWithOrderItems(orderId);
        verify(orderService).calculateAndFinalizeOrderAmounts(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(paymentRepository).save(any(PaymentEntity.class));
        verify(productService, times(2)).decreaseStock(anyLong(), anyInt()); // 2개 상품
        verify(balanceService).use(eq(userId), any(Integer.class));
    }

    @Test
    void 결제_성공_쿠폰_사용() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Long userCouponId = 100L;

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .productId(1L)
                .quantity(1)
                .price(20000)
                .build();

        // OrderEntity 생성 (쿠폰 사용)
        OrderEntity order = OrderEntity.createOrder(userId, userCouponId, Arrays.asList(orderItem));
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "finalAmount", 17000); // 쿠폰 할인 적용
        ReflectionTestUtils.setField(order, "totalAmount", 20000);
        ReflectionTestUtils.setField(order, "userCouponId", userCouponId);

        // PaymentEntity 생성
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(orderId)
                .userId(userId)
                .paidAmount(17000)
                .originalAmount(20000)
                .discountAmount(3000)
                .paymentMethod(PaymentEntity.PaymentMethod.BALANCE)
                .paymentStatus(PaymentEntity.PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentRequest.Process request = new PaymentRequest.Process("BALANCE");

        // Mock 설정
        given(orderService.getOrderWithOrderItems(orderId)).willReturn(order);
        given(orderService.calculateAndFinalizeOrderAmounts(orderId)).willReturn(order);
        given(orderRepository.save(any(OrderEntity.class))).willReturn(order);
        given(paymentRepository.save(any(PaymentEntity.class))).willReturn(payment);

        // ProductService, BalanceService, CouponService Mock 설정
        doNothing().when(productService).decreaseStock(anyLong(), anyInt());
        doNothing().when(balanceService).use(any(Long.class), any(Integer.class));
        doNothing().when(couponService).useCoupon(anyLong());

        // when
        PaymentResponse.Complete result = paymentService.processPayment(userId, orderId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.payment()).isNotNull();

        // 호출 검증
        verify(orderService).getOrderWithOrderItems(orderId);
        verify(orderService).calculateAndFinalizeOrderAmounts(orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(paymentRepository).save(any(PaymentEntity.class));
        verify(productService).decreaseStock(1L, 1); // 1개 상품
        verify(balanceService).use(eq(userId), any(Integer.class));
        verify(couponService).useCoupon(userCouponId); // 쿠폰 사용
    }


}