package com.wayapaychat.paymentgateway.cardservice;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CardTransactionRequest {


    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
    private String amount;
    private String currency;
    private String securityCode;

    private String email;
    private String countryCode;
}
