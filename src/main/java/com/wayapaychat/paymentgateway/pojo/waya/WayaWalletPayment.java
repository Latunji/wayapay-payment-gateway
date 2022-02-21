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
public class WayaWalletPayment {
	
	private String paymentDescription;
	private String merchantId;
	private BigDecimal amount;
	private String wayaPublicKey;
	private String accountNo;
	private String currency;

}
