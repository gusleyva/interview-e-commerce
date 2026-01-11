package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.request.OrderItemUpdateRequest;
import com.ecommerce.repository.OrderItemRepository;
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
public class OrderItemService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<OrderItem> getAllOrderItems() {
        log.debug("Fetching all order items");
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Long id) {
        log.debug("Fetching order item with id: {}", id);
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    public OrderItem updateOrderItem(Long id, OrderItemUpdateRequest request) {
        log.debug("Updating order item with id: {}", id);
        OrderItem orderItem = getOrderItemById(id);
        Product product = orderItem.getProduct();

        int diff = request.getQuantity() - orderItem.getQuantity();

        if (product.getStockQuantity() < diff) {
            throw new IllegalStateException("Not enough stock");
        }

        product.setStockQuantity(product.getStockQuantity() - diff);
        orderItem.setQuantity(request.getQuantity());
        orderItem.setSubtotal(
                orderItem.getUnitPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()))
        );

        return orderItemRepository.save(orderItem);
    }

    public void deleteOrderItem(Long id) {
        log.debug("Deleting order item with id: {}", id);
        OrderItem orderItem = getOrderItemById(id);
        orderItemRepository.delete(orderItem);
    }
}
