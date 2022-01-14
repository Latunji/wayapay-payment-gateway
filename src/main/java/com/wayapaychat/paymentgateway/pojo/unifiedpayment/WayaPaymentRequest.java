package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class WayaPaymentRequest {
	
	@NotBlank(message = "Merchant ID must not be null")
	@Size(min=3, max=30, message = "The Merchant ID '${validatedValue}' must be between {min} and {max} characters long")
	private String merchantId;
	
	@NotBlank(message = "Description must not be null")
	@Size(min=3, max=20, message = "The Description '${validatedValue}' must be between {min} and {max} characters long")
	private String description;
	
	@NotNull(message = "Amount must not Null or Blank")
	@Min(value = 1, message ="Amount must be greater than zero")
	private BigDecimal amount;
	
	@NotNull(message = "Fee must not Null or Blank")
	@Min(value = 1, message ="Fee must be greater than zero")
	private BigDecimal fee;
	
	@Size(min=3, max=3, message = "The currency code '${validatedValue}' must be between {min} and {max} characters long")
	@NotBlank(message = "Currency code must not be null")
	private String currency = "566";
	
	//@NotBlank(message = "Return-Url must not be null")
	//@Size(min=3, max=100, message = "The Return-Url '${validatedValue}' must be between {min} and {max} characters long")
	//private String returnUrl;
	
	@NotBlank(message = "Secret Key must not be null")
	@Size(min=3, max=100, message = "The Secret Key '${validatedValue}' must be between {min} and {max} characters long")
	private String wayaPublicKey;

}
