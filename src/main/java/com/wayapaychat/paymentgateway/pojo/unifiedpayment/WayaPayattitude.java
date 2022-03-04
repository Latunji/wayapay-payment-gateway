package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaPayattitude {

	private String tranId;

	private String cardEncrypt;

	public WayaPayattitude(String tranId, String cardEncrypt) {
		super();
		this.tranId = tranId;
		this.cardEncrypt = cardEncrypt;
	}

	public WayaPayattitude() {
		super();
	}
	

}
