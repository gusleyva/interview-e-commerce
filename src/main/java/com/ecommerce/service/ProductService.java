package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.entity.Product;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.util.PatchUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(Product product) {
        log.debug("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        log.debug("Updating product with id: {}", id);
        Product product = getProductById(id);
        // validateProduct(productDetails);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        return productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getPrice() == null ||
                product.getStockQuantity() == null) {
            throw new IllegalArgumentException("PUT requires all fields: name, price, stockQuantity");
        }
    }

    public Product patchProduct(Long id, Product productDetails) {
        log.debug("Patching product with id: {}", id);
        Product product = getProductById(id);

        PatchUtil.copyNonNullProperties(productDetails, product);

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);
        Product product = getProductById(id);

        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException(
                    "Cannot delete product because it is used in existing orders"
            );
        }

        productRepository.delete(product);
    }
}
