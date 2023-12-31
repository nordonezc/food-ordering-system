package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_APPROVED;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_CANCELED;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_CANCEL_INITIATED;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INITIATED;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_PAID;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.RESTAURANT_IS_NOT_ACTIVE;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ZONE_DATETIME_UTC;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info(String.format(ORDER_INITIATED, order.getId().getValue()));
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_DATETIME_UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info(String.format(ORDER_PAID, order.getId().getValue()));
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_DATETIME_UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info(String.format(ORDER_APPROVED, order.getId().getValue()));

    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info(String.format(ORDER_CANCEL_INITIATED, order.getId().getValue()));
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_DATETIME_UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info(String.format(ORDER_CANCELED, order.getId().getValue()));
    }


    /**
     * TODO HashMaps for products to improve performance
     */
    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        order.getItems().forEach(orderItem -> restaurant.getProducts().forEach(restaurantProduct -> {
            var orderProduct = orderItem.getProduct();
            if (orderProduct.getId().equals(restaurantProduct.getId())) {
                orderProduct.updateWithConfirmedNameAndPrice(
                        restaurantProduct.getName(),
                        restaurantProduct.getPrice());
            }
        }));
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException(String.format(RESTAURANT_IS_NOT_ACTIVE, restaurant.getId().getValue()));
        }
    }
}