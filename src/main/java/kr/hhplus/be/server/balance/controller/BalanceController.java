package kr.hhplus.be.server.balance.controller;

import kr.hhplus.be.server.balance.dto.BalanceRequest;
import kr.hhplus.be.server.balance.dto.BalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/users/{userId}/balance")
public class BalanceController {

    @GetMapping
    public ResponseEntity<BalanceResponse.Balance> getBalance(@PathVariable Long userId) {
        // Mock 데이터 생성
        BalanceResponse.Balance response = new BalanceResponse.Balance(
                userId,
                150000
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/charge")
    public ResponseEntity<BalanceResponse.Balance> chargeBalance(
            @PathVariable Long userId,
            @RequestBody BalanceRequest.Charge request
    ) {
        // Mock 데이터 생성
        BalanceResponse.Balance response = new BalanceResponse.Balance(
                userId,
                request.amount()
        );
        return ResponseEntity.ok(response);
    }

}