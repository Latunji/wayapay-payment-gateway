package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaDecypt {
	private String decryptString;
	private String merchantPublicKey;

	public WayaDecypt(String decryptString, String merchantSecretKey) {
		super();
		this.decryptString = decryptString;
		this.merchantPublicKey = merchantSecretKey;
	}

	public WayaDecypt() {
		super();
	}

}
