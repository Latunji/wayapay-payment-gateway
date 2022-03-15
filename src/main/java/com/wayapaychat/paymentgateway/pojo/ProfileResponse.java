package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timestamp", "message", "status", "data" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

	@JsonProperty("timestamp")
	private String timestamp;

	@JsonProperty("message")
	private String message;

	@JsonProperty("status")
	private boolean status;

	@JsonProperty("data")
	private ProfileData data;

}
