package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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

    @JsonProperty("merchant_id")
    private String merchantId;
}
