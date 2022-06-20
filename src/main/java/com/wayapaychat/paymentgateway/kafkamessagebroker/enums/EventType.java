package com.wayapaychat.paymentgateway.kafkamessagebroker.enums;

public enum EventType {
    CHARGE_EXPIRED_CUSTOMER_SUBSCRIPTION,
    CHARGED_EXPIRED_CUSTOMER_SUBSCRIPTION,
    FAILED_CHARGING_CUSTOMER_SUBSCRIPTION,
    NEW_CUSTOMER_RECURRENT_SUBSCRIPTION_CHARGED, NEW_CUSTOMER_ONE_TIME_PAYMENT_CHARGED, NEW_CUSTOMER_SUBSCRIPTION_CHARGED,
    NEW_MERCHANT_ACCOUNT_CREATED, MERCHANT_LIST_PRICING_CREATED, MERCHANT_PRICING_CREATED, NEW_CORPORATE_ACCOUNT
}