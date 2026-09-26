package dev.aayush.cex.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Order(
        UUID id,
        String clientOrderId,
        String userId,
        String symbol,
        String side,
        String type,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
