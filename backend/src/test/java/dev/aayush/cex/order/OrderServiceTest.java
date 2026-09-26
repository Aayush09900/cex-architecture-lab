package dev.aayush.cex.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    @Test
    void duplicateClientOrderIdReturnsSameOrder() {
        OrderService service = new OrderService();
        CreateOrderRequest request = new CreateOrderRequest(
                "client-1", "user-1", "ETH-USDT", "BUY", "LIMIT",
                new BigDecimal("0.10"), new BigDecimal("3500")
        );

        Order first = service.create(request);
        Order second = service.create(request);

        assertEquals(first.id(), second.id());
        assertEquals(1, service.findAll().size());
    }

    @Test
    void validOrderLifecycleCanReachFilled() {
        OrderService service = new OrderService();
        CreateOrderRequest request = new CreateOrderRequest(
                "client-2", "user-2", "BTC-USDT", "SELL", "LIMIT",
                new BigDecimal("0.01"), new BigDecimal("100000")
        );

        service.create(request);
        service.transition("client-2", OrderStatus.OPEN);
        service.transition("client-2", OrderStatus.PARTIALLY_FILLED);
        Order filled = service.transition("client-2", OrderStatus.FILLED);

        assertEquals(OrderStatus.FILLED, filled.status());
    }

    @Test
    void invalidTransitionIsRejected() {
        OrderService service = new OrderService();
        service.create(new CreateOrderRequest(
                "client-3", "user-3", "ETH-USDT", "BUY", "MARKET",
                new BigDecimal("1"), null
        ));

        assertThrows(IllegalStateException.class,
                () -> service.transition("client-3", OrderStatus.FILLED));
    }
}
