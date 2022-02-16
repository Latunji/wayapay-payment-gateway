package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaEncypt {
	
	private String encryptString;
	
	private String merchantSecretKey;

	public WayaEncypt(String encryptString, String merchantSecretKey) {
		super();
		this.encryptString = encryptString;
		this.merchantSecretKey = merchantSecretKey;
	}

	public WayaEncypt() {
		super();
	}

}
