package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NIPTransferRequest {

    @JsonProperty("myDestinationAccountNumber")
    private String myDestinationAccountNumber;

    @JsonProperty("myDestinationBankCode")
    private String myDestinationBankCode;

    @JsonProperty("myOriginatorName")
    private String myOriginatorName;

    @JsonProperty("myAccountName")
    private String myAccountName;

    @JsonProperty("myNarration")
    private String myNarration;

    @JsonProperty("myPaymentReference")
    private String myPaymentReference;

    @JsonProperty("myAmount")
    private String myAmount;

    @JsonProperty("sourceAccountNo")
    private String sourceAccountNo;

}
