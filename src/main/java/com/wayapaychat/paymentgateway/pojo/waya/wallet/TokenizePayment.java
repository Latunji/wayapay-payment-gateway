package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.Data;

@Data
public class TokenizePayment {

    private String amount;
    private String currency;
    private String customerId;
    private String token;
    private String tokenExpiryDate;
    private String transactionRef;
}
