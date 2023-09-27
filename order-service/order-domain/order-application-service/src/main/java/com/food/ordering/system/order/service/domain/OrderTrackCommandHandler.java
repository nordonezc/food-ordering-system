package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.TRACKING_ORDER_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackCommandHandler {

    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        var trackOrder = orderRepository
                .findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()))
                .orElseThrow(() -> {
                    String message = String.format(TRACKING_ORDER_NOT_FOUND, trackOrderQuery.getOrderTrackingId());
                    log.warn(message);
                    throw new OrderNotFoundException(message);
                });
        return orderDataMapper.orderToTrackOrderResponse(trackOrder);
    }

}