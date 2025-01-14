package com.wayapaychat.paymentgateway.pojo.unifiedpayment;


import lombok.Data;

@Data
public class UnifiedCardRequest {
    private String secretKey;
    private String scheme;
    private String cardHolder;
    private String cardNumber;
    private String cvv;
    private String expiry;
    private String mobile;
    private String pin;
    private String endRecurr;
    private String frequency;
    private boolean isRecurring;
    private Integer OrderExpirationPeriod;
}
