package com.wayapaychat.paymentgateway.pojo.waya;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.wayapaychat.paymentgateway.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentData {	
	
	@JsonProperty("roles")
	private List<String> roles;
	
	@JsonProperty("pinCreated")
	private Boolean pinCreated;
	
	@JsonProperty("user")
	private User user;
	
	@JsonProperty("token")
	private String token;

}
