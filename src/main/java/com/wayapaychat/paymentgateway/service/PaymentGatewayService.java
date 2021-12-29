package com.wayapaychat.paymentgateway.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;

public interface PaymentGatewayService {
	
	/*
	 * PaymentGatewayResponse wemaTransactionQuery(HttpServletRequest request,
	 * WemaTxnQueryRequest tran);
	 * 
	 * PaymentGatewayResponse wemaAllPrefix(HttpServletRequest request);
	 */
	
	PaymentGatewayResponse CardAcquireRequest(HttpServletRequest request, WayaPaymentRequest account, String token);
	
	PaymentGatewayResponse CardAcquirePayment(HttpServletRequest request, WayaCardPayment card);
	
	PaymentGatewayResponse CardAcquireCallback(HttpServletRequest request, WayaPaymentCallback pay);
	
	PaymentGatewayResponse PayAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay);
	
	ResponseEntity<?> GetTransactionStatus(HttpServletRequest req, String tranId);

}
