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

	private String accountNo;

	private String acctName;

	private double clrBalAmt;

	private Boolean walletDefault;

	private String acctCrncyCode;

}
