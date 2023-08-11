package com.food.ordering.system.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public abstract class BaseId<T> {
    private final T value;

    protected BaseId(T value) {
        this.value = value;
    }

}