  package kr.hhplus.be.server.balance.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import kr.hhplus.be.server.balance.domain.entity.UserBalanceEntity;

import java.time.LocalDateTime;

  public record BalanceResponse(
          @Schema(description = "사용자 ID", requiredMode = RequiredMode.REQUIRED)
          Long userId,
          @Schema(description = "현재 잔액", requiredMode = RequiredMode.REQUIRED)
          Integer balance,
          @Schema(description = "수정일", requiredMode = RequiredMode.REQUIRED, format = "date-time")
          LocalDateTime mdfcnDt
  ) {
      public static BalanceResponse from(UserBalanceEntity entity) {
          return new BalanceResponse(entity.getUserId(), entity.getAmount(), entity.getMdfcnDt());
      }
  }

