package dev.aayush.cex.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping
    public List<Order> list() {
        return orderService.findAll();
    }

    @GetMapping("/{clientOrderId}")
    public Order get(@PathVariable String clientOrderId) {
        return orderService.findAll().stream()
            .filter(order -> order.clientOrderId().equals(clientOrderId))
            .findFirst()
            .orElseThrow(OrderNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    void notFound() {}

    static class OrderNotFoundException extends RuntimeException {}
}
