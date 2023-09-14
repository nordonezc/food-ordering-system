package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.CUSTOMER_NOT_FOUND;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ERROR_SAVING_ORDER;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_SAVED;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.RESTAURANT_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        checkCustomer(createOrderCommand.getCustomerId());
        var restaurant = checkRestaurant(createOrderCommand);
        var order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        // TODO fire the event
        var orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
        var orderSaved = saveOrder(order);
        return orderDataMapper.orderToCreateOrderResponse(orderSaved);
    }

    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Optional<Restaurant> restaurant = restaurantRepository
                .findRestaurant(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand));
        if (restaurant.isEmpty()) {
            String errorMessage = String.format(RESTAURANT_NOT_FOUND, createOrderCommand.getRestaurantId());
            log.warn(errorMessage);
            throw new OrderDomainException(errorMessage);
        }

        return restaurant.get();
    }

    private void checkCustomer(UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if (customer.isEmpty()) {
            String errorMessage = String.format(CUSTOMER_NOT_FOUND, customerId);
            log.warn(errorMessage);
            throw new OrderDomainException(errorMessage);
        }
    }

    private Order saveOrder(Order order) {
        var orderStored = orderRepository.save(order);
        if (orderStored == null) {
            log.warn(ERROR_SAVING_ORDER);
            throw new OrderDomainException(ERROR_SAVING_ORDER);
        }
        log.info(String.format(ORDER_SAVED, orderStored.getId()));
        return orderStored;
    }

}