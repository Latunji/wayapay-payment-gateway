package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaEncypt {
	
	private String encryptString;
	private String merchantPublicKey;

	public WayaEncypt(String encryptString, String merchantPublicKey) {
		super();
		this.encryptString = encryptString;
		this.merchantPublicKey = merchantPublicKey;
	}

	public WayaEncypt() {
		super();
	}

}
