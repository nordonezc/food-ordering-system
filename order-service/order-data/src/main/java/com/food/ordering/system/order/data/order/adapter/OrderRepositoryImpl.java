package com.food.ordering.system.order.data.order.adapter;

import com.food.ordering.system.order.data.order.mapper.OrderDataMapper;
import com.food.ordering.system.order.data.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository repository;
    private final OrderDataMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.map(repository.save(mapper.map(order)));
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return repository.findById(trackingId.getValue()).map(mapper::map);
    }
}