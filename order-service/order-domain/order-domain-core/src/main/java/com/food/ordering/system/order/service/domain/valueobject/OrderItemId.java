package com.food.ordering.system.order.service.domain.valueobject;

import com.food.ordering.system.domain.valueobject.BaseId;
import com.food.ordering.system.domain.valueobject.OrderId;

public class OrderItemId extends BaseId<Long> {

    private OrderId orderId;

    public OrderItemId(Long value) {
        super(value);
    }
}