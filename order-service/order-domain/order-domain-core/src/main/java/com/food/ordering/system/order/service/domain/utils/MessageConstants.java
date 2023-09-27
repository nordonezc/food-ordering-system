package com.food.ordering.system.order.service.domain.utils;

public class MessageConstants {

    public static final String ZONE_DATETIME_UTC = "UTC";

    // LOGGER ORDER SERVICE MESSAGES
    public static final String CUSTOMER_NOT_FOUND = "Could not find customer with id: %s";
    public static final String RESTAURANT_NOT_FOUND = "Could not find restaurant with id: %s";

    // LOGGER SERVICE IMPLEMENTATION MESSAGES
    public static final String ORDER_INITIATED = "Order with id: %s is initiated";
    public static final String ORDER_SAVED = "Order with id: %s is saved";
    public static final String ERROR_SAVING_ORDER = "Could not save the order";
    public static final String ORDER_PAID = "Order with id: %s is paid";
    public static final String ORDER_APPROVED = "Order with id: %s is approved";
    public static final String ORDER_CANCEL_INITIATED = "Order with id: %s is requested to cancel";
    public static final String ORDER_CANCELED = "Order with id: %s is canceled";
    public static final String ORDER_PUBLISHED = "Order with id: %s is published";

    public static final String RESTAURANT_IS_NOT_ACTIVE = "Restaurant with id %s is not active";

    // ORDER DOMAIN EXCEPTION
    public static final String ORDER_INCORRECT_FOR_PAY = "Order is not in correct state for pay operation: %s";
    public static final String ORDER_INCORRECT_FOR_APPROVE = "Order is not in correct state for approve operation: %s";

    public static final String ORDER_INCORRECT_FOR_INIT_CANCEL = "Order is not in correct state for initCancel operation: %s";
    public static final String ORDER_INCORRECT_FOR_CANCEL = "Order is not in correct state for cancel operation: %s";
    public static final String TOTAL_PRICE_NOT_EQUAL_TO_ITEMS_PRICE = "Total price: %s is not equal to Order items total: %s!";

    public static final String ITEM_PRICE_NOT_VALID_FOR_PRODUCTS = "Order item price: %s is not valid for product %s";

    public static final String TOTAL_PRICE_MUST_BE_GREATER_THAN_ZERO = "Total price must be greater than zero";

    public static final String ORDER_INCORRECT_STATE_FOR_INIT = "Order is not in correct state for initialization!";

    private MessageConstants() {
    }
}