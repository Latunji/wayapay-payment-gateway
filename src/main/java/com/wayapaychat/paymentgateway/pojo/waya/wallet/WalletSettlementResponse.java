package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timeStamp", "status", "message", "data"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WalletSettlementResponse {

    @JsonProperty("timeStamp")
    private String timeStamp;

    @JsonProperty("status")
    private Boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<WalletTransactionResponse> data;

}
