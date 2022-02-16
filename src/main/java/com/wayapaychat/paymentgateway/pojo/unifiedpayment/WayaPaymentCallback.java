package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaPaymentCallback {
	
	private String tranId;
	
	private String cardEncrypt;

}
