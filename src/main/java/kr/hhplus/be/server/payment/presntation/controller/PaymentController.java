package kr.hhplus.be.server.payment.presntation.controller;

import kr.hhplus.be.server.payment.presntation.dto.PaymentRequest;
import kr.hhplus.be.server.payment.presntation.dto.PaymentResponse;
import kr.hhplus.be.server.payment.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/orders/{orderId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse.Complete> processPayment(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @RequestBody PaymentRequest.Process request
    ) {
        PaymentResponse.Complete response = paymentService.processPayment(userId, orderId, request);
        return ResponseEntity.ok(response);
    }
}