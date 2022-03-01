package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

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
public class CardResponse {
	
	private String tranId;
	
	private String name;

}
