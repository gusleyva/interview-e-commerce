package com.ecommerce.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    @Hidden
    public Map<String, Object> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "E-commerce REST API");
        info.put("version", "1.0.0");
        info.put("status", "running");

        Map<String, String> links = new HashMap<>();
        links.put("swagger", "/swagger-ui/index.html");
        links.put("api-docs", "/api-docs");
        links.put("products", "/api/v1/products");
        links.put("orders", "/api/v1/orders");
        links.put("order-items", "/api/v1/order-items");

        info.put("_links", links);

        return info;
    }
}
