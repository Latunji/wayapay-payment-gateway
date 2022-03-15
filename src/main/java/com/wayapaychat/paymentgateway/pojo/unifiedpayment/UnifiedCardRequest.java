package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

//import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//@JsonPropertyOrder({"secretKey", "scheme", "cardHolder", "cardNumber", "cvv", "expiry", "mobile", "pin"})
public class UnifiedCardRequest {
	
	private String secretKey;
	
	private String scheme;
	
	private String cardHolder;
	
	private String cardNumber;
	
	private String cvv;
	
	private String expiry;
	
	private String mobile;
	
	private String pin;
	
	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getCardHolder() {
		return cardHolder;
	}

	public void setCardHolder(String cardHolder) {
		this.cardHolder = cardHolder;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	@Override
	public String toString() {
		return "UnifiedCardRequest [secretKey=" + secretKey + ", scheme=" + scheme + ", cardHolder=" + cardHolder
				+ ", cardNumber=" + cardNumber + ", cvv=" + cvv + ", expiry=" + expiry + ", mobile=" + mobile + ", pin="
				+ pin + "]";
	}
	

}
