package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaDecypt {

	private String decryptString;

	private String merchantSecretKey;

	public WayaDecypt(String decryptString, String merchantSecretKey) {
		super();
		this.decryptString = decryptString;
		this.merchantSecretKey = merchantSecretKey;
	}

	public WayaDecypt() {
		super();
	}

}
