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
public class WalletQRGenerate {
	
	  private boolean active;
	  
	  private String customerSessionId;
	  
	  private String merchantId;
	  
	  private BigDecimal payableAmount;
	  
	  private String paymentChannel = "QR";
	  
	  private String qrCodeExpiryDate;
	  
	  private String transactionNarration;
	  
	  private long userId;

	  private String merchantEmail;

	  private String firstName;

	  private String surname;

}
