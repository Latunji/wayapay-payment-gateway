package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;

@Data
public class WayaCardPayment {

	private String secretKey;

	private String scheme;

	private String cardNumber;

	private String expiry;

	private String cvv;

	private String cardholder;

	private String mobile;

	private String pin;

}
