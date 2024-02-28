package com.food.ordering.system.order.data.order.mapper;

import com.food.ordering.system.order.data.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.data.order.entity.OrderEntity;
import com.food.ordering.system.order.data.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderDataMapper {

    String FAILURE_MESSAGE_DELIMITER = ",";

    OrderDataMapper ORDER_DATA_MAPPER = Mappers.getMapper(OrderDataMapper.class);

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "customerId.value", target = "customerId")
    @Mapping(source = "restaurantId.value", target = "restaurantId")
    @Mapping(source = "trackingId.value", target = "trackingId")
    @Mapping(source = "deliveryAddress", target = "address")
    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "orderStatus", target = "orderStatus")
    @Mapping(source = "failureMessages", target = "failureMessages", qualifiedByName = "mapFailureMessages")
    OrderEntity map(Order domainOrder);

    OrderAddressEntity map(StreetAddress domainAddress);

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "product.id.value", target = "productId")
    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "subTotal.amount", target = "subTotal")
    OrderItemEntity mapOrderItemDomainToEntity(OrderItem domainListOrderItems);

    @Named("mapFailureMessages")
    default String mapFailureMessages(List<String> failureMessages) {
        return String.join(FAILURE_MESSAGE_DELIMITER,
                Optional.ofNullable(failureMessages)
                        .orElse(Collections.emptyList()));


    }

    @Mapping(target = "id.value", source = "id")
    @Mapping(target = "customerId.value", source = "customerId")
    @Mapping(target = "restaurantId.value", source = "restaurantId")
    @Mapping(target = "trackingId.value", source = "trackingId")
    @Mapping(target = "deliveryAddress", source = "address")
    @Mapping(target = "failureMessages", source = "failureMessages", qualifiedByName = "mapFailureMessages")
    @Mapping(target = "price.amount", source = "price")
    Order map(OrderEntity domainOrder);

    @Mapping(target = "id", expression = "java(new OrderItemId(source.getId()))")
    @Mapping(target = "orderId", expression = "java(new OrderId(source.getOrder().getId()))")
    @Mapping(target = "product", expression = "java(new Product(new ProductId(source.getProductId())))")
    @Mapping(target = "price", expression = "java(new Money(source.getPrice()))")
    @Mapping(target = "subTotal", expression = "java(new Money(source.getSubTotal()))")
    @Mapping(target = "quantity", source = "quantity")
    OrderItem mapOrderItemEntityToDomain(OrderItemEntity source);

    @Named("mapFailureMessages")
    default List<String> mapFailureMessages(String failureMessages) {
        return new ArrayList<>(Arrays.asList(Optional.ofNullable(failureMessages)
                .orElse("").split(FAILURE_MESSAGE_DELIMITER)));
    }

}