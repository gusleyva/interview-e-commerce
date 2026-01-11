package com.ecommerce.controller;

import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.request.OrderCreateRequest;
import com.ecommerce.model.request.OrderItemRequest;
import com.ecommerce.model.request.OrderItemUpdateRequest;
import com.ecommerce.model.response.OrderItemResponse;
import com.ecommerce.model.response.OrderResponse;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(com.ecommerce.exception.GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    ProductRepository productRepository;

    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status(Order.OrderStatus.PENDING)
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .status("PENDING")
                .build();
    }

    @Test
    void getAllOrders_ReturnsListOfOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("John Doe"));

        verify(orderService).getAllOrders();
    }

    @Test
    void createOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");

        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(orderResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldGetOrderItems() throws Exception {
        Long orderId = 1L;

        OrderItem item = OrderItem.builder()
                .id(10L)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .build();

        when(orderService.getOrderItems(orderId))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/orders/{id}/items", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].subtotal").value(200.00));
    }

    @Test
    void shouldAddItemToOrder() throws Exception {
        Long orderId = 1L;

        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(5L);
        request.setQuantity(3);

        OrderItem item = OrderItem.builder()
                .id(20L)
                .quantity(3)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("150.00"))
                .build();

        OrderItemResponse response = new OrderItemResponse(
                20L,
                5L,
                "item-response",
                3,
                new BigDecimal("50.00"),
                new BigDecimal("150.00")
        );

        when(orderService.addItemToOrder(eq(orderId), any(OrderItemRequest.class)))
                .thenReturn(item);

        when(orderMapper.toItemResponse(item))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/{id}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.subtotal").value(150.00));
    }


    @Test
    void shouldUpdateOrderItem() throws Exception {
        Long orderId = 1L;
        Long itemId = 10L;

        OrderItemUpdateRequest request = new OrderItemUpdateRequest();
        request.setQuantity(5);

        OrderItem updatedItem = OrderItem.builder()
                .id(itemId)
                .quantity(5)
                .unitPrice(new BigDecimal("20.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();

        OrderItemResponse response = new OrderItemResponse(
                itemId,
                3L,
                "item-response",
                5,
                new BigDecimal("20.00"),
                new BigDecimal("100.00")
        );

        when(orderService.updateOrderItem(eq(orderId), eq(itemId), any()))
                .thenReturn(updatedItem);

        when(orderMapper.toItemResponse(updatedItem))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/orders/{orderId}/items/{itemId}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.subtotal").value(100.00));
    }
}
