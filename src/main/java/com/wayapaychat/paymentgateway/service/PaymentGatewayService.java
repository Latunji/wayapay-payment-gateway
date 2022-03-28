package com.wayapaychat.paymentgateway.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import org.springframework.http.ResponseEntity;

import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
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
	ResponseEntity<?> WalletPaymentQR(HttpServletRequest request, WayaQRRequest account);
	
	ResponseEntity<?> initiateWalletPayment(HttpServletRequest request, WayaWalletRequest account);
	
	ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, WayaUSSDPayment account, String refNo);
	
	ResponseEntity<?> initiateUSSDTransaction(HttpServletRequest request, WayaUSSDRequest account);

	ResponseEntity<?> walletAuthentication(HttpServletRequest request, WayaAuthenicationRequest account);
	
	ResponseEntity<?> processWalletPayment(HttpServletRequest request, WayaWalletPayment payment, String token);
	
	PaymentGatewayResponse initiateTransaction(HttpServletRequest request, WayaPaymentRequest account, Device device) throws JsonProcessingException;
	
	ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, WayaCardPayment card);
	
	PaymentGatewayResponse processCardTransaction(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay);
	
	PaymentGatewayResponse payAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay);
	
	ResponseEntity<?> getTransactionStatus(HttpServletRequest req, String tranId);
	
	ResponseEntity<?> getTransactionByRef(HttpServletRequest req, String refNo);
	
	WayaTransactionQuery getTransactionStatus(String tranId);
	
	PaymentGatewayResponse encryptCard(HttpServletRequest request, WayaEncypt pay);
	
	PaymentGatewayResponse decryptCard(HttpServletRequest request, WayaDecypt pay);
	
	ResponseEntity<?> queryTranStatus(HttpServletRequest req);
	
	ResponseEntity<?> getMerchantTransactionReport(HttpServletRequest req, String merchantId);
	
	ResponseEntity<?> updateTransactionStatus(HttpServletRequest request, final String refNo, WayaPaymentStatus pay);
	
	ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest req, String merchantId);
	
	ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest req);

	ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest wayaCallbackRequest) throws URISyntaxException;

}
