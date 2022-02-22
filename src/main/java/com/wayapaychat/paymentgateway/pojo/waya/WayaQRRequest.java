package com.wayapaychat.paymentgateway.pojo.waya;

import java.math.BigDecimal;
import java.util.Date;

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
public class WayaQRRequest {

	private String paymentDescription;
	
	private String referenceNo;

	private String merchantId;

	private BigDecimal amount;

	private String wayaPublicKey;

	private String accountNo;

	private String currency;

	private BigDecimal fee;
	
	private Date qrExpiryDate;

}
