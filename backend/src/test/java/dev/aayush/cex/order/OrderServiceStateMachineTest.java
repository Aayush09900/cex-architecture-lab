package dev.aayush.cex.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceStateMachineTest {

    @Test
    void createNormalizesSymbolSideAndType() {
        OrderService service = new OrderService();

        Order order = service.create(new CreateOrderRequest(
                "client-normalized",
                "user-1",
                "eth-usdt",
                "buy",
                "limit",
                new BigDecimal("1.25"),
                new BigDecimal("3500")
        ));

        assertEquals("ETH-USDT", order.symbol());
        assertEquals("BUY", order.side());
        assertEquals("LIMIT", order.type());
        assertEquals(OrderStatus.NEW, order.status());
    }

    @Test
    void unknownOrderCannotTransition() {
        OrderService service = new OrderService();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.transition("missing-client-order", OrderStatus.OPEN)
        );

        assertTrue(error.getMessage().contains("Unknown clientOrderId"));
    }

    @Test
    void terminalOrderCannotTransition() {
        OrderService service = new OrderService();
        CreateOrderRequest request = new CreateOrderRequest(
                "client-terminal",
                "user-1",
                "BTC-USDT",
                "SELL",
                "LIMIT",
                new BigDecimal("0.01"),
                new BigDecimal("100000")
        );

        service.create(request);
        service.transition("client-terminal", OrderStatus.REJECTED);

        assertThrows(
                IllegalStateException.class,
                () -> service.transition("client-terminal", OrderStatus.OPEN)
        );
    }

    @Test
    void newOrderCanBeCancelledButCannotBeFilledDirectly() {
        OrderService service = new OrderService();

        service.create(new CreateOrderRequest(
                "client-new",
                "user-1",
                "BTC-USDT",
                "BUY",
                "MARKET",
                BigDecimal.ONE,
                null
        ));

        service.transition("client-new", OrderStatus.CANCELLED);

        assertEquals(
                OrderStatus.CANCELLED,
                service.findAll().stream()
                        .filter(order -> order.clientOrderId().equals("client-new"))
                        .findFirst()
                        .orElseThrow()
                        .status()
        );
    }
}
