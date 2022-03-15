package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"status",
"message",
"code",
"data"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenAuthResponse {
	
	@JsonProperty("status")
	private Boolean status;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("code")
	private Integer code;
	
	@JsonProperty("data")
	private PaymentData data;

}
