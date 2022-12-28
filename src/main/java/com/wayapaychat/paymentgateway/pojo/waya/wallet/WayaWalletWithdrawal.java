package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WayaWalletWithdrawal {

    @JsonProperty("accountNo")
    private String accountNo;

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("transactionPin")
    private String transactionPin;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("amount")
    private double amount;
}
