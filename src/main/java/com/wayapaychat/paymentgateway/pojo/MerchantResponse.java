package com.wayapaychat.paymentgateway.pojo;

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
public class MerchantResponse {
	
	private String code;
	
	private String date;
	
	private String message;
	
	private MerchantData data;

}
