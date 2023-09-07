package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
public class Restaurant extends AggregateRoot<RestaurantId> {
    private final List<Product> products;
    private boolean active;

    private Restaurant(Builder builder) {
        setId(builder.id);
        products = builder.products;
        active = builder.active;
    }

    public static final class Builder {
        private RestaurantId id;
        private final List<Product> products;
        private boolean active;

        public Builder(List<Product> products) {
            this.products = products;
        }

        public Builder restaurantId(RestaurantId val) {
            id = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}