package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.ToString;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"amount",
"customerAccountNumber",
"eventId",
"paymentReference",
"tranCrncy",
"tranNarration",
"transactionCategory"
})
@Data
@ToString
public class WalletEventPayment {
	
	@JsonProperty("amount")
	private BigDecimal amount;
	
	@JsonProperty("customerAccountNumber")
	private String customerAccountNumber;
	
	@JsonProperty("eventId")
	private String eventId;
	
	@JsonProperty("paymentReference")
	private String paymentReference;
	
	@JsonProperty("tranCrncy")
	private String tranCrncy;
	
	@JsonProperty("tranNarration")
	private String tranNarration;
	
	@JsonProperty("transactionCategory")
	private String transactionCategory;

	public WalletEventPayment(BigDecimal amount, String customerAccountNumber, String eventId, String paymentReference,
			String tranCrncy, String tranNarration, String transactionCategory) {
		super();
		this.amount = amount;
		this.customerAccountNumber = customerAccountNumber;
		this.eventId = eventId;
		this.paymentReference = paymentReference;
		this.tranCrncy = tranCrncy;
		this.tranNarration = tranNarration;
		this.transactionCategory = transactionCategory;
	}

	public WalletEventPayment() {
		super();
	}	
	

}
