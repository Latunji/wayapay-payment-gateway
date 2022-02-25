package com.wayapaychat.paymentgateway.pojo.ussd;

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
public class WayaUSSDPayment {
	
	private String tranId;
	
	private String Status;
	
	private boolean successfailure;
	
	private Date tranDate;
	
	private String merchantId;
	
}
