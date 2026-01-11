package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.request.OrderCreateRequest;
import com.ecommerce.model.request.OrderItemRequest;
import com.ecommerce.model.request.OrderItemUpdateRequest;
import com.ecommerce.model.request.OrderUpdateRequest;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<Order> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));

        return order.getOrderItems();
    }

    public Order createOrder(OrderCreateRequest request) {
        log.debug("Creating new order for customer: {}", request.getCustomerName());

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .status(Order.OrderStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, OrderUpdateRequest request) {
        log.debug("Updating order with id: {}", id);
        Order order = getOrderById(id);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify a finalized order");
        }

        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        log.debug("Deleting order with id: {}", id);
        Order order = getOrderById(id);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be deleted"
            );
        }

        orderRepository.delete(order);

        // TODO - Set to cancelled instead of delete record
        // order.setStatus(OrderStatus.CANCELLED);
        // orderRepository.save(order);
    }

    @Transactional
    public OrderItem addItemToOrder(Long orderId, OrderItemRequest request) {

        Order order = getOrderById(orderId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Not enough stock");
        }

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(
                        product.getPrice()
                                .multiply(BigDecimal.valueOf(request.getQuantity()))
                )
                .build();

        product.setStockQuantity(
                product.getStockQuantity() - request.getQuantity()
        );

        order.addItem(item);

        return item;
    }

    @Transactional
    public OrderItem updateOrderItem(Long orderId, Long itemId, OrderItemUpdateRequest request) {

        Order order = getOrderById(orderId);

        OrderItem item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        int diff = request.getQuantity() - item.getQuantity();

        Product product = item.getProduct();

        if (product.getStockQuantity() < diff) {
            throw new IllegalStateException("Not enough stock");
        }

        product.setStockQuantity(product.getStockQuantity() - diff);

        item.setQuantity(request.getQuantity());
        item.setSubtotal(
                item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()))
        );

        return item;
    }

    @Transactional
    public void deleteOrderItem(Long orderId, Long itemId) {

        Order order = getOrderById(orderId);

        OrderItem item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order item not found with id: " + itemId)
                );

        Product product = item.getProduct();

        product.setStockQuantity(
                product.getStockQuantity() + item.getQuantity()
        );

        order.removeItem(item);
    }


}
