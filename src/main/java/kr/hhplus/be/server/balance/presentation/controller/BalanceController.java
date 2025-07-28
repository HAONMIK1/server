package kr.hhplus.be.server.balance.presentation.controller;

import kr.hhplus.be.server.balance.presentation.dto.BalanceRequest;

import kr.hhplus.be.server.balance.presentation.dto.BalanceResponse;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;
import kr.hhplus.be.server.balance.application.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users/{userId}/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;;

    @GetMapping
    public ResponseEntity<UserBalanceEntity> getBalance(@PathVariable Long userId) {
        UserBalanceEntity result = balanceService.getBalance(userId);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/charge")
    public ResponseEntity<BalanceResponse> chargeBalance(
            @PathVariable Long userId,
            @RequestBody BalanceRequest.Charge request) {
        BalanceResponse result = balanceService.chargeBalance(userId, request.amount());
        return ResponseEntity.ok(result);
    }

}