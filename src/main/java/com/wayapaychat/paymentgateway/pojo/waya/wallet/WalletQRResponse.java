package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.wayapaychat.paymentgateway.pojo.waya.Headers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"headers",
"body",
"statusCodeValue",
"statusCode"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WalletQRResponse {
	
	@JsonProperty("headers")
	private Headers headers;
	
	@JsonProperty("body")
	private String body;
	
	@JsonProperty("statusCodeValue")
	private Integer statusCodeValue;
	
	@JsonProperty("statusCode")
	private String statusCode;
	
	private String name;

}
