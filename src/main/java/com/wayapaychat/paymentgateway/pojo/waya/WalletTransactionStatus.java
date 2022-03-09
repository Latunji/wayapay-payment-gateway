package com.wayapaychat.paymentgateway.pojo.waya;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"OrderId",
"Amount",
"Description",
"Fee",
"Currency",
"Status",
"TranTime"
})
@ToString
@Data
public class WalletTransactionStatus {
	
	@JsonProperty("OrderId")
	private String orderId;
	
	@JsonProperty("Amount")
	private BigDecimal amount;
	
	@JsonProperty("Description")
	private String description;
	
	@JsonProperty("Fee")
	private BigDecimal fee;
	
	@JsonProperty("Currency")
	private String currency;
	
	@JsonProperty("Status")
	private String status;
	
	@JsonProperty("TranTime")
	private String tranTime;

	public WalletTransactionStatus(String orderId, BigDecimal amount, String description, BigDecimal fee,
			String currency, String status) {
		super();
		this.orderId = orderId;
		this.amount = amount;
		this.description = description;
		this.fee = fee;
		this.currency = currency;
		this.status = status;
	}

	public WalletTransactionStatus() {
		super();
	}

}
