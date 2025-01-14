package com.wayapaychat.paymentgateway.pojo.waya;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timestamp", "message", "status", "data" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PinResponse {
	
	@JsonProperty("timestamp")
	private Timestamp timestamp;

	@JsonProperty("message")
	private String message;

	@JsonProperty("status")
	private boolean status;

	@JsonProperty("data")
	private String data;

}
