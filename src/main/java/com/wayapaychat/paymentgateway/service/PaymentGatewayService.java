package com.wayapaychat.paymentgateway.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaDecypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaEncypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaAuthenicationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaQRRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletPayment;

public interface PaymentGatewayService {
	
	/*
	 * PaymentGatewayResponse wemaTransactionQuery(HttpServletRequest request,
	 * WemaTxnQueryRequest tran);
	 * 
	 * PaymentGatewayResponse wemaAllPrefix(HttpServletRequest request);
	 */
	ResponseEntity<?> WalletPaymentQR(HttpServletRequest request, WayaQRRequest account);
	
	ResponseEntity<?> USSDPaymentRequest(HttpServletRequest request, WayaUSSDRequest account);
	
	ResponseEntity<?> WalletPaymentAuthentication(HttpServletRequest request, WayaAuthenicationRequest account);
	
	ResponseEntity<?> ConsumeWalletPayment(HttpServletRequest request, WayaWalletPayment payment, String token);
	
	PaymentGatewayResponse CardAcquireRequest(HttpServletRequest request, WayaPaymentRequest account);
	
	PaymentGatewayResponse CardAcquirePayment(HttpServletRequest request, WayaCardPayment card);
	
	PaymentGatewayResponse CardAcquireCallback(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay);
	
	PaymentGatewayResponse PayAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay);
	
	ResponseEntity<?> GetTransactionStatus(HttpServletRequest req, String tranId);
	
	PaymentGatewayResponse encrypt(HttpServletRequest request, WayaEncypt pay);
	
	PaymentGatewayResponse decrypt(HttpServletRequest request, WayaDecypt pay);

}
