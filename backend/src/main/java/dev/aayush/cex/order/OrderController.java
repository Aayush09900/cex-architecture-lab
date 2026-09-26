package dev.aayush.cex.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Order> list() {
        return service.findAll();
    }

    @PostMapping("/{clientOrderId}/transition/{status}")
    public Order transition(@PathVariable String clientOrderId, @PathVariable OrderStatus status) {
        return service.transition(clientOrderId, status);
    }
}
