# E-commerce REST API

Spring Boot application that provides a REST API for managing an e-commerce platform with products, orders, and order items.

## Important comments

- All endpoints were created with V1 as part of the path in order to handle versioning (`/api/v1...`), I mention it because in the documentation was not included in the specification and endpoints, nonetheless it is a good practice to handle endpoint versioning. 
- OrderItems should be part of Order endpoint to respect Domain-Driven Design principles (avoid mixing to models in separate endpoints), details below.
```
POST   /orders/{id}/items
PUT    /orders/{id}/items/{itemId}
DELETE /orders/{id}/items/{itemId}
```
- Not using interfaces was a decision, I implemented classes, reason:
- - Only one implementation type.
- - Not multiple providers.
- - Tests are repository mocked (not from service).
- If I used interfaces
- - More files.
- - More complexity.
- - Worst navigation.
- If it is necessary to implement interfaces it can be done in the future, example:
```
1️⃣ Multiple implementations
  PaymentService
  ├── StripePaymentService
  └── PaypalPaymentService

2️⃣ Plugins / Strategies
PricingStrategy
├── DefaultPricing
└── DiscountPricing

3️⃣ Infrastructure (ports & adapters)
EmailSender
├── SmtpEmailSender
└── SendgridEmailSender
```

## Features

- **Product Management**: Create, read, update, and delete products
- **Order Management**: Handle customer orders with full CRUD operations
- **Order Item Management**: Manage individual items within orders
- **OpenAPI 3.0 Documentation**: Interactive API documentation with Swagger UI
- **PostgreSQL Database**: Persistent data storage with JPA/Hibernate
- **Docker Support**: Containerized application with Docker Compose
- **Comprehensive Testing**: Unit and integration tests with JUnit and Mockito
- **Error Handling**: Global exception handling with proper HTTP status codes
- **Input Validation**: Bean validation for all entities

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL 16**
- **OpenAPI 3.0 (SpringDoc)**
- **Lombok**
- **Docker & Docker Compose**
- **JUnit 5 & Mockito**
- **Maven**

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## Project Structure

```
ecommerce-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/
│   │   │       ├── EcommerceApplication.java
│   │   │
│   │   │       ├── config/
│   │   │       │   └── DataLoader.java
│   │   │
│   │   │       ├── controller/
│   │   │       │   ├── RootController.java
│   │   │       │   ├── ProductController.java
│   │   │       │   ├── OrderController.java
│   │   │       │   └── OrderItemController.java
│   │   │
│   │   │       ├── service/
│   │   │       │   ├── ProductService.java
│   │   │       │   ├── OrderService.java
│   │   │       │   └── OrderItemService.java
│   │   │
│   │   │       ├── repository/
│   │   │       │   ├── ProductRepository.java
│   │   │       │   ├── OrderRepository.java
│   │   │       │   └── OrderItemRepository.java
│   │   │
│   │   │       ├── model/
│   │   │       │   ├── entity/
│   │   │       │   │   ├── Product.java
│   │   │       │   │   ├── Order.java
│   │   │       │   │   └── OrderItem.java
│   │   │       │   │
│   │   │       │   ├── request/
│   │   │       │   │   ├── ProductRequest.java
│   │   │       │   │   ├── ProductUpdateRequest.java
│   │   │       │   │   ├── OrderCreateRequest.java
│   │   │       │   │   ├── OrderUpdateRequest.java
│   │   │       │   │   ├── OrderItemRequest.java
│   │   │       │   │   └── OrderItemUpdateRequest.java
│   │   │       │   │
│   │   │       │   └── response/
│   │   │       │       ├── ProductResponse.java
│   │   │       │       ├── OrderResponse.java
│   │   │       │       └── OrderItemResponse.java
│   │   │
│   │   │       ├── mapper/
│   │   │       │   ├── ProductMapper.java
│   │   │       │   ├── OrderMapper.java
│   │   │       │   └── OrderItemMapper.java
│   │   │
│   │   │       ├── exception/
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── ErrorResponse.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │
│   │   │       └── util/
│   │   │           └── PatchUtil.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── test/
│       ├── java/
│       │   └── com/ecommerce/
│       │       ├── controller/
│       │       │   ├── ProductControllerTest.java
│       │       │   └── OrderControllerTest.java
│       │       │
│       │       └── service/
│       │           ├── ProductServiceTest.java
│       │           ├── OrderServiceTest.java
│       │           └── OrderItemServiceTest.java
│       │
│       └── resources/
│           └── application-test.properties
│
├── Dockerfile
├── docker-compose.yml
├── run-default.sh
├── run-dev.sh
├── run-prod.sh
├── .env
├── .gitignore
├── pom.xml
└── README.md

```

## Getting Started

### Option 1: Run with Docker Compose (Recommended)

The application supports different profiles:

**Default Mode (Empty Database)**
```bash
docker-compose up --build
```

**Development Mode (With Sample Data)**
```bash
# Linux/Mac
SPRING_PROFILE=dev docker-compose up --build

# Or use the helper script
chmod +x run-dev.sh
./run-dev.sh
```

**Production Mode (Optimized Settings)**
```bash
# Linux/Mac
SPRING_PROFILE=prod docker-compose up --build

# Or use the helper script
chmod +x run-prod.sh
./run-prod.sh
```

This will:
- Start a PostgreSQL database container
- Build the Spring Boot application
- Start the application container
- The API will be available at `http://localhost:8080`

**What each profile does:**
- `default`: Empty database, schema updates automatically (`ddl-auto=update`)
- `dev`: Loads 5 sample products and 2 orders with items (`ddl-auto=create-drop`)
- `prod`: No sample data, only validates schema (`ddl-auto=validate`)

3. **Stop the application**
   ```bash
   docker-compose down
   ```

   To remove volumes as well:
   ```bash
   docker-compose down -v
   ```

### Option 2: Run Locally with Maven

1. **Start PostgreSQL**
   ```bash
   docker run -d \
     --name postgres \
     -e POSTGRES_DB=ecommerce \
     -e POSTGRES_USER=ecommerce_user \
     -e POSTGRES_PASSWORD=ecommerce_pass \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. **Build the application**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR directly:
   ```bash
   java -jar target/ecommerce-api-1.0.0.jar
   ```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Application info UI**: http://localhost:8080/index.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## API Endpoints

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Get all products |
| GET | `/api/v1/products/{id}` | Get product by ID |
| POST | `/api/v1/products` | Create new product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders` | Get all orders |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders/{id}/items` | Get all items for an order |
| POST | `/api/v1/orders` | Create new order |
| POST | `/api/v1/orders/{orderId}/items` | Add item to an order |
| PUT | `/api/v1/orders/{id}` | Update order |
| PUT | `/api/v1/orders/{orderId}/items/{itemId}` | Update item in an order |
| DELETE | `/api/v1/orders/{id}` | Delete order |
| DELETE | `/api/v1/orders/{orderId}/items/{itemId}` | Delete item from an order |


### Order Items
Order items are managed exclusively as a sub-resource of Orders.
/order-items endpoint is available as evidence but should be deprecated or eliminated from swagger.

| Method | Endpoint                | Description |
|--------|-------------------------|-------------|
| GET | `/api/v1/order-items`   | Get all order items |
| GET | `/api/v1/order-items/{id}` | Get order item by ID |
| POST | `/api/v1/order-items`      | Create new order item |
| PUT | `/api/v1/order-items/{id}` | Update order item |
| DELETE | `/api/v1/order-items/{id}` | Delete order item |

## Example API Requests

### Development Mode Sample Data

When running in `dev` profile, you'll have these pre-loaded products:
- Laptop Dell XPS 15 ($1299.99)
- Logitech MX Master 3 ($99.99)
- Mechanical Keyboard RGB ($149.99)
- Samsung 27" 4K Monitor ($399.99)
- Sony WH-1000XM4 ($349.99)

And 2 sample orders from customers "John Doe" and "Jane Smith".

### Create a Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stockQuantity": 50
  }'
```

### Create an Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "status": "PENDING",
    "totalAmount": 1299.99
  }'
```

### Get All Products

```bash
curl http://localhost:8080/api/v1/products
```

## Testing

### Run all tests

```bash
mvn test
```

### Run specific test class

```bash
mvn test -Dtest=ProductServiceTest
```

### Test Coverage

The application includes:
- **Unit Tests**: Service layer tests using Mockito
- **Integration Tests**: Controller tests using MockMvc
- Tests for all CRUD operations
- Exception handling tests
- Validation tests

## Error Handling

The API returns appropriate HTTP status codes and detailed error messages:

- **200 OK**: Successful GET/PUT requests
- **201 Created**: Successful POST requests
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Validation errors
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server errors

Example error response:
```json
{
   "timestamp": "2024-01-09T10:30:00",
   "status": 404,
   "error": "Not Found",
   "message": "Product not found with id: 999",
   "path": "/api/v1/products/999"
}
```

## Database Schema

### Products Table
- `id` (BIGINT, Primary Key)
- `name` (VARCHAR(100), NOT NULL)
- `description` (VARCHAR(500))
- `price` (DECIMAL(10,2), NOT NULL)
- `stock_quantity` (INTEGER, NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Orders Table
- `id` (BIGINT, Primary Key)
- `customer_name` (VARCHAR(100), NOT NULL)
- `customer_email` (VARCHAR(100), NOT NULL)
- `status` (VARCHAR(20), NOT NULL)
- `total_amount` (DECIMAL(10,2), NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Order Items Table
- `id` (BIGINT, Primary Key)
- `order_id` (BIGINT, Foreign Key)
- `product_id` (BIGINT, Foreign Key)
- `quantity` (INTEGER, NOT NULL)
- `unit_price` (DECIMAL(10,2), NOT NULL)
- `subtotal` (DECIMAL(10,2), NOT NULL)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

## Configuration

Key configuration properties in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://postgres:5432/ecommerce
spring.datasource.username=ecommerce_user
spring.datasource.password=ecommerce_pass

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8080
```

## Health Check

The application includes a health check endpoint:

```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Port Already in Use

If port 8080 or 5432 is already in use:

```bash
# Change application port in application.properties
server.port=8081

# Change PostgreSQL port in docker-compose.yml
ports:
  - "5433:5432"
```

### Check application logs

On this example we search for log "create table" in the app logs

```bash
docker-compose logs app | grep -i "create table"
```

### Database Connection Issues

Ensure PostgreSQL is running and accessible:

```bash
docker-compose logs postgres
docker-compose logs app | grep -i "create table"
```

### Build Issues

Clean and rebuild:

```bash
mvn clean install -U
```

### Connect to PostgreSQL 

PostgreSQL is running, is accessible, then:

```bash
docker exec -it ecommerce-postgres psql -U ecommerce_user -d ecommerce -c "\dt"

docker exec -it ecommerce-postgres psql -U ecommerce_user -d ecommerce -c "SELECT * FROM products LIMIT 1;"

\dt                           # List tables
\d products                   # Table structure
\d+ products                  # Table structure with indexes
SELECT * FROM products;       # Check data
\q                            # Exit
```

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Contact: ovatleyva@gmail.com