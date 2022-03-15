package com.wayapaychat.paymentgateway.pojo.ussd;

import java.math.BigDecimal;

import com.wayapaychat.paymentgateway.pojo.Customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WayaUSSDRequest {

	private String paymentDescription;

	private String referenceNo;

	private String merchantId;

	private BigDecimal amount;

	private String wayaPublicKey;

	private String phoneNo;

	private String currency;

	private BigDecimal fee;
	
	private Customer customer;

}
