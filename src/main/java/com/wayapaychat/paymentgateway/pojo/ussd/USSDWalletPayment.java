package com.wayapaychat.paymentgateway.pojo.ussd;

import java.math.BigDecimal;

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
public class USSDWalletPayment {
	
	private BigDecimal tranAmt;
	
	private String paymentDescription;
	
	private String referenceNo;
	
	private String vendorTranId;
	
	private BigDecimal feeAmt;

}
