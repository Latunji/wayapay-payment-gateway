package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import java.math.BigDecimal;

public class UnifiedPaymentRequest {
	
	private String id;
	
	private String description;
	
	private BigDecimal amount;
	
	private BigDecimal fee;
	
	private String currency;
	
	private String returnUrl;
	
	private String secretKey;
	
	private String scheme;
	
	private String vendorId;
	
	private String parameter;
	
	private int count;
	
	private String subMerchantId;
	
	private String subMerchantName;
	
	private String subMerchantCity;
	
	private String subMerchantCountryCode;
	
	private String subMerchantPostalCode;
	
	private String subMerchantStreetAddress;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

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

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}

	public String getSubMerchantCity() {
		return subMerchantCity;
	}

	public void setSubMerchantCity(String subMerchantCity) {
		this.subMerchantCity = subMerchantCity;
	}

	public String getSubMerchantCountryCode() {
		return subMerchantCountryCode;
	}

	public void setSubMerchantCountryCode(String subMerchantCountryCode) {
		this.subMerchantCountryCode = subMerchantCountryCode;
	}

	public String getSubMerchantPostalCode() {
		return subMerchantPostalCode;
	}

	public void setSubMerchantPostalCode(String subMerchantPostalCode) {
		this.subMerchantPostalCode = subMerchantPostalCode;
	}

	public String getSubMerchantStreetAddress() {
		return subMerchantStreetAddress;
	}

	public void setSubMerchantStreetAddress(String subMerchantStreetAddress) {
		this.subMerchantStreetAddress = subMerchantStreetAddress;
	}

	public UnifiedPaymentRequest(String id, String description, BigDecimal amount, BigDecimal fee, String currency,
			String returnUrl, String secretKey, int count) {
		super();
		this.id = id;
		this.description = description;
		this.amount = amount;
		this.fee = fee;
		this.currency = currency;
		this.returnUrl = returnUrl;
		this.secretKey = secretKey;
		this.scheme = "";
		this.vendorId = "";
		this.parameter = "";
		this.count = count;
		this.subMerchantId = "";
		this.subMerchantName = "";
		this.subMerchantCity = "";
		this.subMerchantCountryCode = "";
		this.subMerchantPostalCode = "";
		this.subMerchantStreetAddress = "";
	}

	public UnifiedPaymentRequest() {
		super();
	}

	@Override
	public String toString() {
		return "UnifiedPaymentRequest [id=" + id + ", description=" + description + ", amount=" + amount + ", fee="
				+ fee + ", currency=" + currency + ", returnUrl=" + returnUrl + ", secretKey=" + secretKey + ", scheme="
				+ scheme + ", vendorId=" + vendorId + ", parameter=" + parameter + ", count=" + count
				+ ", subMerchantId=" + subMerchantId + ", subMerchantName=" + subMerchantName + ", subMerchantCity="
				+ subMerchantCity + ", subMerchantCountryCode=" + subMerchantCountryCode + ", subMerchantPostalCode="
				+ subMerchantPostalCode + ", subMerchantStreetAddress=" + subMerchantStreetAddress + "]";
	}
	
	

}
