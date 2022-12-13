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

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("transaction_pin")
    private String transactionPin;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("amount")
    private String amount;
}
