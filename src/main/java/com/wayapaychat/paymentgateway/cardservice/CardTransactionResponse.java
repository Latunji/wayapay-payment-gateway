package com.wayapaychat.paymentgateway.cardservice;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardTransactionResponse {

    private String queryId;
    private String transactionId;
    private String submitTimeUtc;
    private String status;
    private String accessToken;
    private String referenceId;
    private String url;
    private String errorMessage;
    private String errorReason;
    private String cardType;
    private String callBackUrl;
    private String redirectUrl;
    private String customerUrl;
    private String is3dsEnabled;
    private String expiration;
    private String identifier;

//-----------ISW RESPONSE -------
    private String processorResponseCode;
    private String message;

    private String paymentId;
    private String amount;
    private String responseCode;
    private String plainTextSupportMessage;

    private String authTransactionId;

    @JsonProperty("MD")
    @SerializedName("MD")
    private String md;
    @JsonProperty("ACSUrl")
    @SerializedName("ACSUrl")
    private String acsUrl;
    @JsonProperty("TermUrl")
    @SerializedName("TermUrl")
    private String termUrl;
    @JsonProperty("PaReq")
    @SerializedName("PaReq")
    private String paReq;
    private String eciFlag;






}
