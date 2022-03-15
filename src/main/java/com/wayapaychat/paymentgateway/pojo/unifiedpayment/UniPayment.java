package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UniPayment {
	
	private String callbackResponse;
	
	private String callbackUrl;

	public UniPayment(String callbackResponse, String callbackUrl) {
		super();
		this.callbackResponse = callbackResponse;
		this.callbackUrl = callbackUrl;
	}

	public UniPayment() {
		super();
	}

}
