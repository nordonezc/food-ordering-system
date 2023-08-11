package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@SuperBuilder
@Getter
public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }

    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException(
                    String.format("Order is not in correct state for pay operation: %d", orderStatus));
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException(
                    String.format("Order is not in correct state for approve operation: %d", orderStatus));
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException(
                    String.format("Order is not in correct state for initCancel operation: %d", orderStatus));
        }
        orderStatus = OrderStatus.CANCELLING;
    }

    public void cancel() {
        if (orderStatus != OrderStatus.PENDING || orderStatus != OrderStatus.CANCELLING) {
            throw new OrderDomainException(
                    String.format("Order is not in correct state for cancel operation: %s", orderStatus.name()));
        }
        orderStatus = OrderStatus.CANCELLED;
    }

    public void updateFailureMessage(List<String> failureMessages) {
        if (this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream().filter(m -> !m.isEmpty()).toList());
        }
        if(this.failureMessages == null){
            this.failureMessages = failureMessages;
        }
    }

    private void validateItemsPrice() {
        var orderItemsTotal = items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException(
                    String.format("Total price: %s is not equal to Order items total: %s!",
                            price.getAmount(),
                            orderItemsTotal.getAmount()));
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if (!orderItem.isPriceValid()) {
            throw new OrderDomainException(
                    String.format("Order item price: %s is not valid for product %s",
                            orderItem.getPrice().getAmount(),
                            orderItem.getProduct().getId().getValue()));
        }

    }

    private void validateTotalPrice() {
        if (price == null || price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero");
        }
    }

    private void validateInitialOrder() {
        if (orderStatus != null || getId() != null) {
            throw new OrderDomainException("Order is not in correct state for initialization!");
        }
    }

    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }
}