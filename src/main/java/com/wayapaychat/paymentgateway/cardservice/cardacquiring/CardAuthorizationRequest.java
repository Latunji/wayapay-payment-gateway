package com.wayapaychat.paymentgateway.cardservice.cardacquiring;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CardAuthorizationRequest {

    private String transactionId;
    private String otp;
}
