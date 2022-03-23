package com.wayapaychat.paymentgateway.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import org.springframework.http.ResponseEntity;

import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.ussd.USSDWalletPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaAuthenicationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaPaymentStatus;
import com.wayapaychat.paymentgateway.pojo.waya.WayaQRRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletRequest;
import org.springframework.mobile.device.Device;

import java.net.URISyntaxException;

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
	
	PaymentGatewayResponse CardAcquireRequest(HttpServletRequest request, WayaPaymentRequest account, Device device) throws JsonProcessingException;
	
	PaymentGatewayResponse CardAcquirePayment(HttpServletRequest request, WayaCardPayment card);
	
	PaymentGatewayResponse CardAcquireCallback(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay);
	
	PaymentGatewayResponse PayAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay);
	
	ResponseEntity<?> GetTransactionStatus(HttpServletRequest req, String tranId);
	
	ResponseEntity<?> GetReferenceStatus(HttpServletRequest req, String refNo);
	
	WayaTransactionQuery GetTransactionStatus(String tranId);
	
	PaymentGatewayResponse encrypt(HttpServletRequest request, WayaEncypt pay);
	
	PaymentGatewayResponse decrypt(HttpServletRequest request, WayaDecypt pay);
	
	ResponseEntity<?> QueryTranStatus(HttpServletRequest req);
	
	ResponseEntity<?> QueryMerchantTranStatus(HttpServletRequest req, String merchantId);
	
	ResponseEntity<?> postRefStatus(HttpServletRequest request, final String refNo, WayaPaymentStatus pay);
	
	ResponseEntity<?> QueryMerchantRevenue(HttpServletRequest req, String merchantId);
	
	ResponseEntity<?> QueryRevenue(HttpServletRequest req);

	ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest wayaCallbackRequest) throws URISyntaxException;

}
