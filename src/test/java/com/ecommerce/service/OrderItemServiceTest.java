package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.model.request.OrderItemUpdateRequest;
import com.ecommerce.repository.OrderItemRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private Product product;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .stockQuantity(6)
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
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(product.getPrice())
                .build();
    }

    @Test
    void getAllOrderItems_ReturnsList() {
        when(orderItemRepository.findAll()).thenReturn(List.of(orderItem));

        List<OrderItem> result = orderItemService.getAllOrderItems();

        assertEquals(1, result.size());
        verify(orderItemRepository).findAll();
    }

    @Test
    void getOrderItemById_Existing_ReturnsItem() {
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));

        OrderItem result = orderItemService.getOrderItemById(1L);

        assertEquals(1L, result.getId());
        verify(orderItemRepository).findById(1L);
    }

    @Test
    void getOrderItemById_NotFound_ThrowsException() {
        when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderItemService.getOrderItemById(99L));
    }

    @Test
    void updateOrderItem_IncreaseQuantity_AdjustsStock() {
        var productLaptop = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("1000.00"))
                .stockQuantity(10)
                .build();

        var orderJohn = Order.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(Order.OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        var orderItemLaptop = OrderItem.builder()
                .id(1L)
                .order(orderJohn)
                .product(productLaptop)
                .quantity(2)
                .unitPrice(productLaptop.getPrice())
                .build();

        OrderItemUpdateRequest request = new OrderItemUpdateRequest(4);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItemLaptop));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItemLaptop);

        OrderItem result = orderItemService.updateOrderItem(1L, request);

        assertEquals(8, productLaptop.getStockQuantity());
        assertEquals(4, result.getQuantity());
    }

    @Test
    void updateOrderItem_InsufficientStock_ThrowsException() {
        OrderItemUpdateRequest request = new OrderItemUpdateRequest(20);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));

        assertThrows(IllegalStateException.class,
                () -> orderItemService.updateOrderItem(1L, request));
    }

    @Test
    void deleteOrderItem_Existing_DeletesSuccessfully() {
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));

        orderItemService.deleteOrderItem(1L);

        verify(orderItemRepository).delete(orderItem);
    }
}

