package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WayaCallbackRequest {
	
	private String trxId;
	
	private boolean approved;
	
	private String status;

}
