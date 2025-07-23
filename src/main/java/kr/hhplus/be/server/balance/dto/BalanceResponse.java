  package kr.hhplus.be.server.balance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

  public class BalanceResponse {
      public record Balance(
              @Schema(description = "사용자 ID", requiredMode = RequiredMode.REQUIRED)
              long userId,
              @Schema(description = "충전된 금액", requiredMode = RequiredMode.REQUIRED)
              int balance

      ) {}
  }