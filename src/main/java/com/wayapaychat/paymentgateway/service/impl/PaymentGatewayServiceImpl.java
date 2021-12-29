package com.wayapaychat.paymentgateway.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.pojo.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.SuccessResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedCardRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.MerchantProxy;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.UnifiedPaymentProxy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

	//private WemaBankProxy proxy;

	@Autowired
	UnifiedPaymentProxy uniPaymentProxy;

	@Autowired
	MerchantProxy merchantProxy;
	
	@Autowired
	PaymentGatewayRepository paymentGatewayRepo;

	/*
	 * @Autowired public PaymentGatewayServiceImpl(WemaBankProxy proxy) { this.proxy
	 * = proxy; }
	 * 
	 * @Override public PaymentGatewayResponse
	 * wemaTransactionQuery(HttpServletRequest request, WemaTxnQueryRequest tran) {
	 * log.debug("Request received --- {}", "wemaTransactionQuery", tran);
	 * 
	 * PaymentGatewayResponse response = new
	 * ErrorResponse(Constant.ERROR_PROCESSING); try { ServiceResponse wemaRes =
	 * proxy.transactionQuery(tran.getSessionid(), tran.getCraccount(),
	 * tran.getAmount(), tran.getTxndate()); if (wemaRes.getResponseCode() ==
	 * Constant.SUCCESS_CODE) { return new
	 * SuccessResponse(Constant.OPERATION_SUCCESS, wemaRes.getData()); } else {
	 * return new ErrorResponse(wemaRes.getStatusMessage()); }
	 * 
	 * } catch (Exception ex) { log.error("Error occurred - Name Enquiry : ", ex); }
	 * return response; }
	 */

	/*
	 * @Override public PaymentGatewayResponse wemaAllPrefix(HttpServletRequest
	 * request) { PaymentGatewayResponse response = new
	 * ErrorResponse(Constant.ERROR_PROCESSING); try { ServiceResponse wemaRes =
	 * proxy.getAllPrefix(); if (wemaRes.getResponseCode() == Constant.SUCCESS_CODE)
	 * { return new SuccessResponse(Constant.OPERATION_SUCCESS, wemaRes.getData());
	 * } else { return new ErrorResponse(wemaRes.getStatusMessage()); }
	 * 
	 * } catch (Exception ex) { log.error("Error occurred - Name Enquiry : ", ex); }
	 * return response; }
	 */

	@Override
	public PaymentGatewayResponse CardAcquireRequest(HttpServletRequest request, WayaPaymentRequest account,
			String token) {
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Unprocess Transaction", null);
		try {
			
			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getId());
			log.info("Merchant: " + merchant.toString());
			PaymentGateway payment = new PaymentGateway();
			Date dte=new Date();
		    long milliSeconds = dte.getTime();
		    String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getId());
			payment.setDescription(account.getDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getIsoCurrencyCode());
			payment.setReturnUrl(account.getReturnUrl());
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getSecretKey(), secretKey);
			payment.setSecretKey(vt);
			
			String tranId = uniPaymentProxy.postUnified(account);
			if (!tranId.isBlank()) {
				response = new PaymentGatewayResponse(true, "Success Transaction", tranId);
				payment.setTranId(tranId);
				payment.setTranDate(LocalDate.now());
				payment.setRcre_time(LocalDateTime.now());
				paymentGatewayRepo.save(payment);
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return response;
	}

	@Override
	public PaymentGatewayResponse CardAcquirePayment(HttpServletRequest request, WayaCardPayment card) {
		UnifiedCardRequest cardReq = new UnifiedCardRequest();
		if (card.getScheme().equalsIgnoreCase("Amex") || card.getScheme().equalsIgnoreCase("Mastercard")
				|| card.getScheme().equalsIgnoreCase("Visa")) {
			cardReq.setSecretKey(card.getSecretKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(card.getCardNumber());
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(card.getCvv());
			cardReq.setCardholder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
		} else if (card.getScheme().equalsIgnoreCase("Verve")) {
			cardReq.setSecretKey(card.getSecretKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(card.getCardNumber());
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(card.getCvv());
			cardReq.setCardholder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
		} else if (card.getScheme().equalsIgnoreCase("PayAttitude")) {
			cardReq.setSecretKey(card.getSecretKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(card.getCardNumber());
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(card.getCvv());
			cardReq.setCardholder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
		}
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Encrypt Card fail", null);
		String encryptData = uniPaymentProxy.encryptPaymentDataAccess(cardReq);
		if (!encryptData.isBlank()) {
			response = new PaymentGatewayResponse(true, "Success Encrypt", encryptData);
		}
		return response;
	}

	@Override
	public PaymentGatewayResponse CardAcquireCallback(HttpServletRequest request, WayaPaymentCallback pay) {
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Callback fail", null);
		String callReq = uniPaymentProxy.getPaymentStatus(pay.getTranId(), pay.getCardEncrypt());
		if (!callReq.isBlank()) {
			response = new PaymentGatewayResponse(true, "Success Encrypt", callReq);
		}
		return response;
	}

	@Override
	public PaymentGatewayResponse PayAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay) {
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "PayAttitude Callback fail", null);
		WayaTransactionQuery callReq = uniPaymentProxy.postPayAttitude(pay);
		if (callReq != null) {
			response = new PaymentGatewayResponse(true, "Success Encrypt", callReq);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> GetTransactionStatus(HttpServletRequest req, String tranId) {
		WayaTransactionQuery response = uniPaymentProxy.transactionQuery(tranId);
		String input = "NJOKU EMMANUEL IFEANYI";
		final String secretKey = "ssshhhhhhhhhhh!!!!";
		try {
			String vt = UnifiedPaymentProxy.getDataEncrypt(input, secretKey);
			log.info(vt);
			log.info(UnifiedPaymentProxy.getDataDecrypt(vt, secretKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response == null) {
			return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
	}

}
