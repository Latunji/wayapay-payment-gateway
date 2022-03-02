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
import com.wayapaychat.paymentgateway.pojo.ussd.USSDWalletPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaAuthenicationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaQRRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletRequest;

public interface PaymentGatewayService {
	
	/*
	 * PaymentGatewayResponse wemaTransactionQuery(HttpServletRequest request,
	 * WemaTxnQueryRequest tran);
	 * 
	 * PaymentGatewayResponse wemaAllPrefix(HttpServletRequest request);
	 */
	ResponseEntity<?> WalletPaymentQR(HttpServletRequest request, WayaQRRequest account);
	
	ResponseEntity<?> PostWalletPayment(HttpServletRequest request, WayaWalletRequest account);
	
	ResponseEntity<?> USSDPayment(HttpServletRequest request, WayaUSSDPayment account, String refNo);
	
	ResponseEntity<?> USSDPaymentRequest(HttpServletRequest request, WayaUSSDRequest account);
	
	ResponseEntity<?> USSDWalletPayment(HttpServletRequest request, USSDWalletPayment account);
	
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
