package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.paymentgateway.pojo.waya.Customer;
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
public class TransactionStatusResponse {
	
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
	
	private String productName;
	
	private String businessName;
	
	private Customer customer;

	private String merchantId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonProperty("TransactionDate")
	private LocalDateTime tranDate;

	public TransactionStatusResponse(String orderId, BigDecimal amount, String description, BigDecimal fee,
									 String currency, String status, String productName, String businessName, Customer customer, String merchantId, LocalDateTime tranDate) {
		super();
		this.orderId = orderId;
		this.amount = amount;
		this.description = description;
		this.fee = fee;
		this.currency = currency;
		this.status = status;
		this.productName = productName;
		this.businessName = businessName;
		this.customer = customer;
		this.merchantId = merchantId;
		this.tranDate = tranDate;
	}

	public TransactionStatusResponse() {
		super();
	}

}
