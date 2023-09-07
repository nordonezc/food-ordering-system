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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ITEM_PRICE_NOT_VALID_FOR_PRODUCTS;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INCORRECT_FOR_APPROVE;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INCORRECT_FOR_CANCEL;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INCORRECT_FOR_INIT_CANCEL;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INCORRECT_FOR_PAY;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_INCORRECT_STATE_FOR_INIT;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.TOTAL_PRICE_MUST_BE_GREATER_THAN_ZERO;
import static com.food.ordering.system.order.service.domain.utils.MessageConstants.TOTAL_PRICE_NOT_EQUAL_TO_ITEMS_PRICE;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
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
                    String.format(ORDER_INCORRECT_FOR_PAY, orderStatus));
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException(
                    String.format(ORDER_INCORRECT_FOR_APPROVE, orderStatus));
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException(
                    String.format(ORDER_INCORRECT_FOR_INIT_CANCEL, orderStatus));
        }
        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessage(failureMessages);
    }

    public void cancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PENDING || orderStatus != OrderStatus.CANCELLING) {
            throw new OrderDomainException(
                    String.format(ORDER_INCORRECT_FOR_CANCEL, orderStatus.name()));
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessage(failureMessages);
    }

    public void updateFailureMessage(List<String> failureMessages) {
        if (this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream().filter(m -> !m.isEmpty()).toList());
        }
        if (this.failureMessages == null) {
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
                    String.format(TOTAL_PRICE_NOT_EQUAL_TO_ITEMS_PRICE,
                            price.getAmount(),
                            orderItemsTotal.getAmount()));
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        if (!orderItem.isPriceValid()) {
            throw new OrderDomainException(
                    String.format(ITEM_PRICE_NOT_VALID_FOR_PRODUCTS,
                            orderItem.getPrice().getAmount(),
                            orderItem.getProduct().getId().getValue()));
        }

    }

    private void validateTotalPrice() {
        if (price == null || price.isGreaterThanZero()) {
            throw new OrderDomainException(TOTAL_PRICE_MUST_BE_GREATER_THAN_ZERO);
        }
    }

    private void validateInitialOrder() {
        if (orderStatus != null || getId() != null) {
            throw new OrderDomainException(ORDER_INCORRECT_STATE_FOR_INIT);
        }
    }

    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }
}