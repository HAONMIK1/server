package kr.hhplus.be.server.order.presentation.controller;

import kr.hhplus.be.server.order.presentation.dto.OrderRequest;
import kr.hhplus.be.server.order.presentation.dto.OrderResponse;
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
    public ResponseEntity<OrderResponse.Detail> createOrder(
            @PathVariable Long userId,
            @RequestBody OrderRequest.Create request
    ) {
        OrderResponse.Detail response = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(response);
    }

}