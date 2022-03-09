package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"Order Id",
"Amount",
"Description",
"Convenience Fee",
"Currency",
"Status",
"Card Holder",
"PAN",
"TranTime",
"TranDateTime",
"StatusDescription"
})
@ToString
@Data
public class WayaTransactionQuery {
	
	@JsonProperty("Order Id")
	private String orderId;
	
	@JsonProperty("Amount")
	private String amount;
	
	@JsonProperty("Description")
	private String description;
	
	@JsonProperty("Convenience Fee")
	private String convenienceFee;
	
	@JsonProperty("Currency")
	private String currency;
	
	@JsonProperty("Status")
	private String status;
	
	@JsonProperty("Card Holder")
	private Object cardHolder;
	
	@JsonProperty("PAN")
	private Object pan;
	
	@JsonProperty("TranTime")
	private String tranTime;
	
	@JsonProperty("TranDateTime")
	private String tranDateTime;
	
	@JsonProperty("StatusDescription")
	private String statusDescription;
	

	@JsonProperty("Order Id")
	public String getOrderId() {
	return orderId;
	}

	@JsonProperty("Order Id")
	public void setOrderId(String orderId) {
	this.orderId = orderId;
	}

	@JsonProperty("Amount")
	public String getAmount() {
	return amount;
	}

	@JsonProperty("Amount")
	public void setAmount(String amount) {
	this.amount = amount;
	}

	@JsonProperty("Description")
	public String getDescription() {
	return description;
	}

	@JsonProperty("Description")
	public void setDescription(String description) {
	this.description = description;
	}

	@JsonProperty("Convenience Fee")
	public String getConvenienceFee() {
	return convenienceFee;
	}

	@JsonProperty("Convenience Fee")
	public void setConvenienceFee(String convenienceFee) {
	this.convenienceFee = convenienceFee;
	}

	@JsonProperty("Currency")
	public String getCurrency() {
	return currency;
	}

	@JsonProperty("Currency")
	public void setCurrency(String currency) {
	this.currency = currency;
	}

	@JsonProperty("Status")
	public String getStatus() {
	return status;
	}

	@JsonProperty("Status")
	public void setStatus(String status) {
	this.status = status;
	}

	@JsonProperty("Card Holder")
	public Object getCardHolder() {
	return cardHolder;
	}

	@JsonProperty("Card Holder")
	public void setCardHolder(Object cardHolder) {
	this.cardHolder = cardHolder;
	}

	@JsonProperty("PAN")
	public Object getPan() {
	return pan;
	}

	@JsonProperty("PAN")
	public void setPan(Object pan) {
	this.pan = pan;
	}

	@JsonProperty("TranTime")
	public String getTranTime() {
	return tranTime;
	}

	@JsonProperty("TranTime")
	public void setTranTime(String tranTime) {
	this.tranTime = tranTime;
	}

	@JsonProperty("TranDateTime")
	public String getTranDateTime() {
	return tranDateTime;
	}

	@JsonProperty("TranDateTime")
	public void setTranDateTime(String tranDateTime) {
	this.tranDateTime = tranDateTime;
	}

	@JsonProperty("StatusDescription")
	public String getStatusDescription() {
	return statusDescription;
	}

	@JsonProperty("StatusDescription")
	public void setStatusDescription(String statusDescription) {
	this.statusDescription = statusDescription;
	}

}
