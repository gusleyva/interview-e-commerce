package com.ecommerce.controller;

import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.request.OrderCreateRequest;
import com.ecommerce.model.request.OrderItemRequest;
import com.ecommerce.model.request.OrderItemUpdateRequest;
import com.ecommerce.model.request.OrderUpdateRequest;
import com.ecommerce.model.response.OrderItemResponse;
import com.ecommerce.model.response.OrderResponse;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        // TODO - Implement pagination
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderItems(id));
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemResponse> addItemToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequest request
    ) {
        OrderItem item = orderService.addItemToOrder(orderId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderMapper.toItemResponse(item));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest order) {
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderItemResponse> updateOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody OrderItemUpdateRequest request
    ) {
        OrderItem item = orderService.updateOrderItem(orderId, itemId, request);
        return ResponseEntity.ok(orderMapper.toItemResponse(item));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Delete item from order")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) {
        orderService.deleteOrderItem(orderId, itemId);
        return ResponseEntity.noContent().build();
    }
}
