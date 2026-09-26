package dev.aayush.cex.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String clientOrderId,
        @NotBlank String userId,
        @NotBlank String symbol,
        @NotBlank String side,
        @NotBlank String type,
        @NotNull @DecimalMin("0.00000001") BigDecimal quantity,
        BigDecimal price
) {}
