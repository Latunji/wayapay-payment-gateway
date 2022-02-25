package com.wayapaychat.paymentgateway.pojo.waya;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"amount",
"creditEventId",
"debitEventId",
"paymentReference",
"tranCrncy",
"tranNarration",
"transactionCategory"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WalletOfficePayment {
	
	@JsonProperty("amount")
	private BigDecimal amount;
	
	@JsonProperty("creditEventId")
	private String creditEventId;
	
	@JsonProperty("debitEventId")
	private String debitEventId;
	
	@JsonProperty("paymentReference")
	private String paymentReference;
	
	@JsonProperty("tranCrncy")
	private String tranCrncy;
	
	@JsonProperty("tranNarration")
	private String tranNarration;
	
	@JsonProperty("transactionCategory")
	private String transactionCategory;

}
