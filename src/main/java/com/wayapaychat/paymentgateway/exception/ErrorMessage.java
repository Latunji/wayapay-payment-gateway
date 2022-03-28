package com.wayapaychat.paymentgateway.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class ErrorMessage {

	private Date timeStamp;
	private String message;

	public ErrorMessage(Date timeStamp, String message) {
		this.timeStamp = timeStamp;
		this.message = message;
	}

}
