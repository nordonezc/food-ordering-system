package com.food.ordering.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AggregateRoot<ID> extends BaseEntity<ID> {
}