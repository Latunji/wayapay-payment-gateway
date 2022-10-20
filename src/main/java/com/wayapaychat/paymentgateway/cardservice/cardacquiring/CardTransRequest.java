package com.wayapaychat.paymentgateway.cardservice.cardacquiring;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CardTransRequest {


    private String transactionId;
    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
    private String amount;
    private String currency;
    private String securityCode;
//    private String pin;
    private String customerId;

    private String mode;
    private String merchantId;
    private String merchantName;

}
