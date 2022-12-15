package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class WalletData {
	
	@JsonProperty("accountNo")
	private String accountNo;
	
	@JsonProperty("acct_name")
	private String acctName;
	
	@JsonProperty("clr_bal_amt")
	private BigDecimal clrBalAmt;
	
	@JsonProperty("walletDefault")
	private Boolean walletDefault;
	
	@JsonProperty("acct_crncy_code")
	private String acctCrncyCode;

}
