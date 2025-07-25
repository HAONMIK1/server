package kr.hhplus.be.server.order.presentation.controller;

import kr.hhplus.be.server.order.presentation.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.presentation.controller.dto.OrderResponse;
import kr.hhplus.be.server.order.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/orders/{orderId}/process")
@RequiredArgsConstructor
public class OrderController{
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse.Payment> createOrder(
            @PathVariable Long userId,
            @RequestBody OrderRequest.Create request
    ) {
        OrderResponse.Payment response = orderService.createOrder(userId, request.orderId());
        return ResponseEntity.ok(response);
    }

}