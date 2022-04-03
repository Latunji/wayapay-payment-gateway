package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WayaCardPayment {
    @NotNull(message = "scheme can no be null")
    private String wayaPublicKey;
    @NotNull(message = "scheme can no be null")
    private String scheme;
    @NotNull(message = "encryptCardNo can no be null")
    private String encryptCardNo;
    @NotNull(message = "expiry can no be null")
    private String expiry;
    private String cardholder;
    private String mobile;
    private String pin;
    private String deviceInformation = "{}";
    private String tranId;
    private boolean recurrentPayment;
    private RecurrentPaymentPojo recurrentPaymentDTO;
}
