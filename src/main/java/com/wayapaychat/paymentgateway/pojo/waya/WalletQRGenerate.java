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
public class WalletQRGenerate {
	
	  private boolean active;
	  
	  private String customerSessionId;
	  
	  private String merchantId;
	  
	  private BigDecimal payableAmount;
	  
	  private String paymentChannel = "QR";
	  
	  private Date qrCodeExpiryDate;
	  
	  private String transactionNarration;
	  
	  private long userId;

}
