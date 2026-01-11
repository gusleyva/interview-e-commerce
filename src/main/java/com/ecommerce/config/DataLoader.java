package com.ecommerce.config;

import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.OrderItem;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Bean
    @Profile("dev")
    public CommandLineRunner loadData() {
        return args -> {
            log.info("Loading sample data...");

            Product laptop = Product.builder()
                    .name("Laptop Dell XPS 15")
                    .description("High-performance laptop with Intel i7")
                    .price(new BigDecimal("1299.99"))
                    .stockQuantity(50)
                    .build();

            Product mouse = Product.builder()
                    .name("Logitech MX Master 3")
                    .description("Wireless ergonomic mouse")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(100)
                    .build();

            Product keyboard = Product.builder()
                    .name("Mechanical Keyboard RGB")
                    .description("Gaming keyboard with RGB lighting")
                    .price(new BigDecimal("149.99"))
                    .stockQuantity(75)
                    .build();

            Product monitor = Product.builder()
                    .name("Samsung 27\" 4K Monitor")
                    .description("4K UHD monitor with HDR")
                    .price(new BigDecimal("399.99"))
                    .stockQuantity(30)
                    .build();

            Product headphones = Product.builder()
                    .name("Sony WH-1000XM4")
                    .description("Noise-cancelling wireless headphones")
                    .price(new BigDecimal("349.99"))
                    .stockQuantity(60)
                    .build();

            productRepository.save(laptop);
            productRepository.save(mouse);
            productRepository.save(keyboard);
            productRepository.save(monitor);
            productRepository.save(headphones);

            log.info("Created {} products", 5);

            Order order1 = Order.builder()
                    .customerName("John Doe")
                    .customerEmail("john.doe@example.com")
                    .status(Order.OrderStatus.PENDING)
                    .orderItems(new ArrayList<>())
                    .build();

            OrderItem item1 = OrderItem.builder()
                    .order(order1)
                    .product(laptop)
                    .quantity(1)
                    .unitPrice(laptop.getPrice())
                    .subtotal(laptop.getPrice())
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .order(order1)
                    .product(mouse)
                    .quantity(2)
                    .unitPrice(mouse.getPrice())
                    .subtotal(mouse.getPrice().multiply(new BigDecimal("2")))
                    .build();

            OrderItem item3 = OrderItem.builder()
                    .order(order1)
                    .product(keyboard)
                    .quantity(1)
                    .unitPrice(keyboard.getPrice())
                    .subtotal(keyboard.getPrice())
                    .build();

            order1.getOrderItems().add(item1);
            order1.getOrderItems().add(item2);
            order1.getOrderItems().add(item3);

            orderRepository.save(order1);

            Order order2 = Order.builder()
                    .customerName("Jane Smith")
                    .customerEmail("jane.smith@example.com")
                    .status(Order.OrderStatus.PROCESSING)
                    .orderItems(new ArrayList<>())
                    .build();

            OrderItem item4 = OrderItem.builder()
                    .order(order2)
                    .product(monitor)
                    .quantity(1)
                    .unitPrice(monitor.getPrice())
                    .subtotal(monitor.getPrice())
                    .build();

            OrderItem item5 = OrderItem.builder()
                    .order(order2)
                    .product(headphones)
                    .quantity(1)
                    .unitPrice(headphones.getPrice())
                    .subtotal(headphones.getPrice())
                    .build();

            order2.getOrderItems().add(item4);
            order2.getOrderItems().add(item5);

            orderRepository.save(order2);

            log.info("Created {} orders with order items", 2);
            log.info("Sample data loaded successfully!");
        };
    }
}