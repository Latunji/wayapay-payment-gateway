package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"code",
"message",
"data",
"date"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCustomer {
	
	@JsonProperty("code")
	private String code;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("data")
	private CustomerData data;
	
	@JsonProperty("date")
	private String date;

}
