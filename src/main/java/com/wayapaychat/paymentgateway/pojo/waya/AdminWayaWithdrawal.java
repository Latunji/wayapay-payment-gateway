package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminWayaWithdrawal {


    @JsonProperty("accountNo")
    private String accountNo;

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("sourceAccount")
    private String sourceAccount;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("amount")
    private String amount;
}
