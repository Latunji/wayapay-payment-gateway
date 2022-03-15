package com.wayapaychat.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class User {
	
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("isEmailVerified")
	private Boolean isEmailVerified;
	
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	
	@JsonProperty("firstName")
	private String firstName;
	
	@JsonProperty("lastName")
	private String lastName;
	
	@JsonProperty("isAdmin")
	private Boolean isAdmin;
	
	@JsonProperty("isPhoneVerified")
	private Boolean isPhoneVerified;

}
