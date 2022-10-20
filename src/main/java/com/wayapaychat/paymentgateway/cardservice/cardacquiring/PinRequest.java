package com.wayapaychat.paymentgateway.cardservice.cardacquiring;


import lombok.Data;

@Data
public class PinRequest {
    private String transactionId;
    private String pin;
}
