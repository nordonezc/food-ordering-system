package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import static com.food.ordering.system.order.service.domain.utils.MessageConstants.ORDER_PUBLISHED;

@Slf4j
@Component
public class ApplicationDomainEventPublisher implements
        ApplicationEventPublisherAware,
        DomainEventPublisher<OrderCreatedEvent> {

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(OrderCreatedEvent event) {
        this.eventPublisher.publishEvent(event);
        log.info(String.format(ORDER_PUBLISHED, event.getOrder().getId()));
    }
}