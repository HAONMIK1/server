package kr.hhplus.be.server.order.service;

import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.OrderEntity;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @InjectMocks
    private OrderService orderService;

    private ProductEntity createProduct(Long id, int price) {
        ProductEntity product = new ProductEntity();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "price", price);
        return product;
    }

    @Test
    void 주문_성공() {
        // given
        Long userId = 1L;
        Long userCouponId = 1L;
        OrderRequest.Create request = new OrderRequest.Create(List.of(new OrderRequest.OrderItem(1L, 2)), userCouponId);
        ProductEntity product = createProduct(1L, 10000);

        given(productService.getProduct(1L)).willReturn(product);
        given(orderRepository.save(any(OrderEntity.class))).willAnswer(invocation -> {
            OrderEntity savedOrder = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedOrder, "id", 1L);
            return savedOrder;
        });

        // when
        orderService.placeOrder(userId, request);

        // then
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void 주문_완료_성공() {
        // given
        Long orderId = 1L;
        OrderEntity order = OrderEntity.createOrder(1L, null, List.of());
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        orderService.completeOrder(orderId);

        // then
        verify(orderRepository).findById(orderId);
    }
}