package com.dnnthanh.marketplace.be.order.api.domain.constant;

import lombok.experimental.UtilityClass;

/** Technical/business constants owned by the Order bounded context. */
@UtilityClass
public class OrderConstants {
    public static final String ORDER_NUMBER_PREFIX = "ORD-";
    public static final String PAYMENT_INBOX_CONSUMER = "be-order-payment-outcome";
    public static final String FULFILLMENT_INBOX_CONSUMER = "be-order-fulfillment-outcome";
    public static final int UNPAID_EXPIRY_MINUTES = 30;
    public static final int EXPIRY_BATCH_SIZE = 100;
}
