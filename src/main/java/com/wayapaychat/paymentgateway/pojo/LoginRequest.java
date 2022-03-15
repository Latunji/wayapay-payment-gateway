package com.wayapaychat.paymentgateway.pojo;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequest {
	
	private String emailOrPhoneNumber;
	
	private String password;

}
