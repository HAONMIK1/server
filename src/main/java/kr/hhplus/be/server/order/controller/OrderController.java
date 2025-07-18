package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.order.dto.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/orders/{orderId}")
public class OrderController{

    @PostMapping
    public ResponseEntity<OrderResponse.Detail> getOrder(
            @PathVariable Long orderId,@PathVariable Long userId
    ) {
        List<OrderResponse.OrderItem> items = List.of(
                new OrderResponse.OrderItem(1L, 1L, 1, 29900, LocalDateTime.now()),
                new OrderResponse.OrderItem(2L, 2L, 1, 29900, LocalDateTime.now())
        );
        OrderResponse.Payment payment = new OrderResponse.Payment(
                1L, 52000, "CARD", "결제완료", LocalDateTime.now(), LocalDateTime.now()
        );
        OrderResponse.Detail response = new OrderResponse.Detail(
                orderId, userId, 1L, 59800, 7800, 52000, "결제완료",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                items, payment
        );
        return ResponseEntity.ok(response);
    }

}