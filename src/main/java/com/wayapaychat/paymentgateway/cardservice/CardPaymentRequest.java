package com.wayapaychat.paymentgateway.cardservice;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardPaymentRequest {

    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
    private String merchantId;
    private String merchantName;
    private String email;
    private String redirectUrl;
    private String customerUrl;
    private String amount;
    private String currency;
    private String securityCode;
    private String countryCode;
    private String cardProcessor;
    private String customerId;
    private String transactionId;
    private String mode;
}
