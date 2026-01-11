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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .stockQuantity(10)
                .build();

        order = Order.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(Order.OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(product.getPrice())
                .subtotal(new BigDecimal("2000.00"))
                .build();

        order.addItem(orderItem);
    }

    @Test
    void getAllOrders_ReturnsListOfOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_ExistingId_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertEquals("John Doe", result.getCustomerName());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(99L)
        );
    }

    @Test
    void createOrder_ValidRequest_CreatesOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrder_PendingOrder_UpdatesCustomerData() {
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order updated = orderService.updateOrder(1L, request);

        assertEquals("Jane Doe", updated.getCustomerName());
        assertEquals("jane@example.com", updated.getCustomerEmail());
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_NonPendingOrder_ThrowsException() {
        order.setStatus(Order.OrderStatus.COMPLETED);

        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.updateOrder(1L, request)
        );

        assertEquals("Cannot modify a finalized order", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_ExistingId_DeletesOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void addItemToOrder_ValidRequest_AddsItemAndAdjustsStock() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderItem result = orderService.addItemToOrder(1L, request);

        assertEquals(7, product.getStockQuantity());
        assertEquals(2, order.getOrderItems().size());
        assertEquals(3, result.getQuantity());
    }

    @Test
    void addItemToOrder_InsufficientStock_ThrowsException() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(1L);
        request.setQuantity(50);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class,
                () -> orderService.addItemToOrder(1L, request));
    }

    @Test
    void addItemToOrder_OrderNotFound_ThrowsException() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.addItemToOrder(1L, request));
    }

    @Test
    void updateOrderItem_IncreaseQuantity_AdjustsStock() {
        OrderItemUpdateRequest request = new OrderItemUpdateRequest(4);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItem result = orderService.updateOrderItem(1L, 1L, request);

        assertEquals(8, product.getStockQuantity());
        assertEquals(4, result.getQuantity());
    }

    @Test
    void updateOrderItem_InsufficientStock_ThrowsException() {
        OrderItemUpdateRequest request = new OrderItemUpdateRequest(50);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.updateOrderItem(1L, 1L, request));
    }

    @Test
    void deleteOrderItem_RemovesItemAndRestoresStock() {

        Product product = Product.builder()
                .id(1L)
                .stockQuantity(5)
                .price(new BigDecimal("100"))
                .build();

        OrderItem item = OrderItem.builder()
                .id(10L)
                .product(product)
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderItems(new ArrayList<>(List.of(item)))
                .build();

        item.setOrder(order);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderItem(1L, 10L);

        assertEquals(7, product.getStockQuantity());
        assertTrue(order.getOrderItems().isEmpty());
    }


}

