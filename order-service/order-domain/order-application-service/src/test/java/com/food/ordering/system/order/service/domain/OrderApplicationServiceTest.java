package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ITEM_PRICE_NOT_VALID_FOR_PRODUCTS;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_CREATED_SUCCESSFULLY;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.RESTAURANT_IS_NOT_ACTIVE;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.TOTAL_PRICE_NOT_EQUAL_TO_ITEMS_PRICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link SpringBootTest} is for initialize the Test Spring Beans
 * {@link TestInstance} to create a single instance
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderDataMapper orderDataMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();

    private final BigDecimal EXPECTED_TOTAL_PRICE = new BigDecimal("200.00");
    private final BigDecimal INVALID_TOTAL_PRICE = new BigDecimal("100.00");

    private final BigDecimal EXPECTED_PRODUCT_PRICE = new BigDecimal("50.00");
    private final BigDecimal INVALID_PRODUCT_PRICE = new BigDecimal("60.00");

    @BeforeAll
    public void initAll() {
        createOrderCommand = validOrderCommand();
        createOrderCommandWrongPrice = wrongTotalPriceOrderCommand();
        createOrderCommandWrongProductPrice = wrongProductPriceOrderCommand();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any())).thenReturn(order);
    }

    @BeforeEach
    public void initEach() {
        createOrderCommand = validOrderCommand();
        Restaurant restaurant = new Restaurant.Builder(
                List.of(sampleProduct("product-1"),
                        sampleProduct("product-2")))
                .active(true)
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .build();

        var restaurantDB = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        when(restaurantRepository.findRestaurant(restaurantDB)).thenReturn(Optional.of(restaurant));
    }

    @Test
    void createOrder_whenValidInput_thenValidOutput() {
        var actualResponse = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(OrderStatus.PENDING, actualResponse.getOrderStatus());
        assertEquals(ORDER_CREATED_SUCCESSFULLY, actualResponse.getMessage());
        assertNotNull(actualResponse.getOrderTrackingId());
    }

    @Test
    void createOrder_whenWrongTotalPrice_thenThrowOrderDomainException() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
        assertEquals(String.format(TOTAL_PRICE_NOT_EQUAL_TO_ITEMS_PRICE,
                        INVALID_TOTAL_PRICE,
                        EXPECTED_TOTAL_PRICE),
                orderDomainException.getMessage());
    }

    @Test
    void createOrder_whenWrongProductPrice_thenThrowOrderDomainException() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
        assertEquals(String.format(ITEM_PRICE_NOT_VALID_FOR_PRODUCTS,
                        INVALID_PRODUCT_PRICE,
                        PRODUCT_ID),
                orderDomainException.getMessage());
    }

    @Test
    void createOrder_whenInactiveRestaurant_thenThrowOrderDomainException() {
        Restaurant restaurant = new Restaurant.Builder(
                List.of(sampleProduct("product-1"),
                        sampleProduct("product-2")))
                .active(false)
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .build();
        var restaurantDB = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        when(restaurantRepository.findRestaurant(restaurantDB)).thenReturn(Optional.of(restaurant));


        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
        assertEquals(String.format(RESTAURANT_IS_NOT_ACTIVE, RESTAURANT_ID),
                orderDomainException.getMessage());
    }

    private Product sampleProduct(String name) {
        return new Product(
                new ProductId(PRODUCT_ID),
                name,
                new Money(EXPECTED_PRODUCT_PRICE));
    }

    private CreateOrderCommand validOrderCommand() {
        return CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(createSampleAddress())
                .price(EXPECTED_TOTAL_PRICE)
                .orderItems(List.of(
                        createSampleProduct(1, EXPECTED_PRODUCT_PRICE),
                        createSampleProduct(3, EXPECTED_PRODUCT_PRICE)))
                .build();
    }

    private CreateOrderCommand wrongTotalPriceOrderCommand() {
        return CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(createSampleAddress())
                .price(INVALID_TOTAL_PRICE)
                .orderItems(List.of(
                        createSampleProduct(1, EXPECTED_PRODUCT_PRICE),
                        createSampleProduct(3, EXPECTED_PRODUCT_PRICE)))
                .build();
    }

    private CreateOrderCommand wrongProductPriceOrderCommand() {
        return CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(createSampleAddress())
                .price(INVALID_TOTAL_PRICE)
                .orderItems(List.of(
                        createSampleProduct(1, INVALID_PRODUCT_PRICE),
                        createSampleProduct(3, EXPECTED_PRODUCT_PRICE)))
                .build();
    }

    private static OrderAddress createSampleAddress() {
        return OrderAddress.builder()
                .city("City sample")
                .postalCode("Postal Code")
                .street("Street sample")
                .build();
    }

    private OrderItem createSampleProduct(int quantity, BigDecimal productPrice) {
        return OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(quantity)
                .price(productPrice)
                .subtotal(productPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}