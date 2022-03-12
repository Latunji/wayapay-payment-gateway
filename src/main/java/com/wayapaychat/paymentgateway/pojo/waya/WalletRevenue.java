package com.wayapaychat.paymentgateway.pojo.waya;

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
	
	private int totalSuccess;
	
	private int totalFail;
	
	private int totalAbandon;
	
	private int totalPending;
	
	private int totalSettle;
	
	private int totalRefund;

}
