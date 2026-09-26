package dev.aayush.cex.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        return orders.computeIfAbsent(request.clientOrderId(), id ->
            new Order(UUID.randomUUID().toString(), id, request.symbol(), request.side(), request.quantity(), "NEW", Instant.now()));
    }

    @GetMapping("/{clientOrderId}")
    public Order get(@PathVariable String clientOrderId) {
        Order order = orders.get(clientOrderId);
        if (order == null) throw new OrderNotFoundException();
        return order;
    }

    public record CreateOrderRequest(
        @NotBlank @Pattern(regexp="[A-Z0-9_-]{3,40}") String clientOrderId,
        @NotBlank @Pattern(regexp="[A-Z0-9]{2,20}-[A-Z0-9]{2,20}") String symbol,
        @NotBlank @Pattern(regexp="BUY|SELL") String side,
        @DecimalMin(value="0.00000001") BigDecimal quantity) {}

    public record Order(String id, String clientOrderId, String symbol, String side, BigDecimal quantity, String status, Instant createdAt) {}
    static class OrderNotFoundException extends RuntimeException {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    void notFound() {}
}
