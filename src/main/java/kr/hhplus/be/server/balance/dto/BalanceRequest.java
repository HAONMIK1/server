package kr.hhplus.be.server.balance.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.math.BigDecimal;
public class BalanceRequest {
    public record Charge(
            @Schema(description = "충전할 금액", requiredMode = RequiredMode.REQUIRED)
            int amount
    ) {}
}