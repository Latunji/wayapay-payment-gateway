package com.wayapaychat.paymentgateway.enumm;

public enum EventType {
    CHARGE_EXPIRED_CUSTOMER_SUBSCRIPTION,
    CHARGED_EXPIRED_CUSTOMER_SUBSCRIPTION,
    FAILED_CHARGING_CUSTOMER_SUBSCRIPTION,
    NEW_CUSTOMER_RECURRENT_SUBSCRIPTION_CHARGED, NEW_CUSTOMER_ONE_TIME_PAYMENT_CHARGED, EMAIL, NEW_CUSTOMER_SUBSCRIPTION_CHARGED,
    SMS,
    IN_APP,
    MERCHANT_SETTLEMENT_COMPLETED, CUSTOMER_SUBSCRIPTION,
    PENDING_TRANSACTION_SETTLEMENT;
}