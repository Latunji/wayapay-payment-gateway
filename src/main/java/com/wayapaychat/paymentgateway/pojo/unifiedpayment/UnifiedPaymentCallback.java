package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UnifiedPaymentCallback {
	
	private String Id;
	
	private String Mid;
	
	private String Payload;

	public UnifiedPaymentCallback(String id, String mid, String payload) {
		super();
		Id = id;
		Mid = mid;
		Payload = payload;
	}

	public UnifiedPaymentCallback() {
		super();
	}

}
