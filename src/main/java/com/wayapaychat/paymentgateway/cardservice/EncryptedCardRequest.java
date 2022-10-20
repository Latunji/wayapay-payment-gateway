package com.wayapaychat.paymentgateway.cardservice;


import lombok.Data;

@Data
public class EncryptedCardRequest {


    private String data;
    private String merchantId;
    private String phone;
    private String firstName;
    private String lastName;
    private String redirectUrl;
    private String customerUrl;

}
