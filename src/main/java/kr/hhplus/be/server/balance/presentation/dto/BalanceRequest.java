package kr.hhplus.be.server.balance.presentation.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class BalanceRequest {
    public record Charge(
            @Schema(description = "사용자 ID", requiredMode = RequiredMode.REQUIRED)
            Long userId,
            @Schema(description = "충전 금액", requiredMode = RequiredMode.REQUIRED)
            Integer amount
    ) {}
}