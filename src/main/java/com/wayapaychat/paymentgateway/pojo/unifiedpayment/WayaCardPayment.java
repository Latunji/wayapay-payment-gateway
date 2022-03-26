package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaCardPayment {
    private String wayaPublicKey;
    private String scheme;
    private String encryptCardNo;
    private String expiry;
    private String cardholder;
    private String mobile;
    private String pin;
    private String deviceInformation = "{}";
    private String transactionId;
}
