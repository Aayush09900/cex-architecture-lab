package dev.aayush.cex.order;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {
    private final Map<String, Order> byClientOrderId = new ConcurrentHashMap<>();

    public Order create(CreateOrderRequest request) {
        return byClientOrderId.computeIfAbsent(request.clientOrderId(), ignored -> {
            Instant now = Instant.now();
            return new Order(
                    UUID.randomUUID(),
                    request.clientOrderId(),
                    request.userId(),
                    request.symbol().toUpperCase(),
                    request.side().toUpperCase(),
                    request.type().toUpperCase(),
                    request.quantity(),
                    request.price(),
                    OrderStatus.NEW,
                    now,
                    now
            );
        });
    }

    public List<Order> findAll() {
        return byClientOrderId.values().stream().toList();
    }

    public Order transition(String clientOrderId, OrderStatus next) {
        Order current = byClientOrderId.get(clientOrderId);
        if (current == null) {
            throw new IllegalArgumentException("Unknown clientOrderId: " + clientOrderId);
        }
        if (!isAllowed(current.status(), next)) {
            throw new IllegalStateException("Invalid transition: " + current.status() + " -> " + next);
        }
        Order updated = new Order(
                current.id(), current.clientOrderId(), current.userId(), current.symbol(),
                current.side(), current.type(), current.quantity(), current.price(),
                next, current.createdAt(), Instant.now()
        );
        byClientOrderId.put(clientOrderId, updated);
        return updated;
    }

    private boolean isAllowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case NEW -> to == OrderStatus.OPEN || to == OrderStatus.REJECTED || to == OrderStatus.CANCELLED;
            case OPEN -> to == OrderStatus.PARTIALLY_FILLED || to == OrderStatus.FILLED || to == OrderStatus.CANCELLED;
            case PARTIALLY_FILLED -> to == OrderStatus.FILLED || to == OrderStatus.CANCELLED;
            case FILLED, CANCELLED, REJECTED -> false;
        };
    }
}
