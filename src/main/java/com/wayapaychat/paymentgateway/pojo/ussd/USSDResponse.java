package com.wayapaychat.paymentgateway.pojo.ussd;

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
public class USSDResponse {
	
	private String refNo;
	
	private String name;

}
