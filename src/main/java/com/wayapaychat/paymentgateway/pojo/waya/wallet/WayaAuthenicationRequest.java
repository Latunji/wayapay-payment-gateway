package com.wayapaychat.paymentgateway.pojo.waya.wallet;

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
public class WayaAuthenicationRequest {
	
	private String emailOrPhoneNumber;
	
	private String password;

}
