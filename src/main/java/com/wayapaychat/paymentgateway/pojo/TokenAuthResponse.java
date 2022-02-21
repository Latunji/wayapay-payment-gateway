package com.wayapaychat.paymentgateway.pojo;

import java.util.Date;

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
public class TokenAuthResponse {
	
	private boolean status;
	private String message;
	private String token;

}
