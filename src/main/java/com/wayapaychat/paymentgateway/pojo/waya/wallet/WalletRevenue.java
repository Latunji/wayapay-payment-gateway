package com.wayapaychat.paymentgateway.pojo.waya.wallet;

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
public class WalletRevenue {
	
	private BigDecimal grossAmount;
	
	private BigDecimal netAmount;
	
	private int totalTransaction;
	
	private int totalSuccess;
	
	private int totalFailed;
	
	private int totalAbandoned;
	
	private int totalPending;
	
	private int totalSettled;
	
	private int totalRefunded;
	
	private String merchantId;

}
