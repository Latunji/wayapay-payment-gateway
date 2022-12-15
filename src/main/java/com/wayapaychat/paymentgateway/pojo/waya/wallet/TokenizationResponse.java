package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.Data;

@Data
public class TokenizationResponse {

    private String balance;
    private String cardType;
    private String panLast4Digits;
    private String token;
    private String tokenExpiryDate;
    private String transactionRef;
}
