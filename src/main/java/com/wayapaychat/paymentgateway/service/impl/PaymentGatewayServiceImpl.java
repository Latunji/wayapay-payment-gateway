package com.wayapaychat.paymentgateway.service.impl;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.pojo.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.MerchantData;
import com.wayapaychat.paymentgateway.pojo.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.SuccessResponse;
import com.wayapaychat.paymentgateway.pojo.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UniPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedCardRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaDecypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaEncypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.MerchantProxy;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.UnifiedPaymentProxy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

	// private WemaBankProxy proxy;

	@Autowired
	UnifiedPaymentProxy uniPaymentProxy;

	@Autowired
	MerchantProxy merchantProxy;
	
	@Autowired
	AuthApiClient authProxy;

	@Autowired
	PaymentGatewayRepository paymentGatewayRepo;
	
	@Value("${service.name}")
	private String username;

	@Value("${service.pass}")
	private String passSecret;

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
	public PaymentGatewayResponse CardAcquireRequest(HttpServletRequest request, WayaPaymentRequest account) {
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Unprocess Transaction", null);
		try {
			LoginRequest auth = new LoginRequest();
			auth.setEmailOrPhoneNumber(username);
			auth.setPassword(passSecret);
			TokenAuthResponse authToken = authProxy.UserLogin(auth);
			log.info("Response: " + authToken.toString());
			if(!authToken.isStatus()) {
				return new PaymentGatewayResponse(false, "Unable to authenticate Demon User", null);
			}
			String token = authToken.getToken();
			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getMerchantId());
			if (!merchant.getCode().equals("00")) {
				return new PaymentGatewayResponse(false, "Merchant id doesn't exist", null);
			}
			log.info("Merchant: " + merchant.toString());
			MerchantData sMerchant = merchant.getData();
			if (sMerchant.getMerchantKeyMode().equals("TEST")) {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
					return new PaymentGatewayResponse(false, "Invalid merchant key", null);
				}
			} else {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
					return new PaymentGatewayResponse(false, "Invalid merchant key", null);
				}
			}
			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getMerchantId());
			payment.setDescription(account.getDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getCurrency());
			payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
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
		String keygen = null;
		if (card.getWayaPublicKey().contains("TEST")) {
			keygen = card.getWayaPublicKey().replace("WAYAPUBK_TEST_0x", "");
			log.info(keygen);
		} else {
			keygen = card.getWayaPublicKey().replace("WAYAPUBK_PROD_0x", "");
			log.info(keygen);
		}
		UnifiedCardRequest cardReq = new UnifiedCardRequest();
		if (card.getScheme().equalsIgnoreCase("Amex") || card.getScheme().equalsIgnoreCase("Mastercard")
				|| card.getScheme().equalsIgnoreCase("Visa")) {
			String vt = UnifiedPaymentProxy.getDataDecrypt(card.getEncryptCardNo(), keygen);
			log.info(vt);
			if (vt == null || vt.equals("")) {
				return new PaymentGatewayResponse(false, "Invalid Encryption", null);
			}
			if (vt.length() < 16) {
				return new PaymentGatewayResponse(false, "Invalid Card", null);
			}
			String[] mt = vt.split(Pattern.quote("|"));
			String cardNo = mt[0];
			String cvv = mt[1];
			cardReq.setSecretKey(card.getWayaPublicKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(cardNo);
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(cvv);
			cardReq.setCardHolder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
			log.info("Card Info: " + cardReq.toString());
		} else if (card.getScheme().equalsIgnoreCase("Verve")) {
			String vt = UnifiedPaymentProxy.getDataDecrypt(card.getEncryptCardNo(), keygen);
			log.info(vt);
			if (vt == null || vt.equals("")) {
				return new PaymentGatewayResponse(false, "Invalid Encryption", null);
			}
			if (vt.length() < 16) {
				return new PaymentGatewayResponse(false, "Invalid Card", null);
			}
			String[] mt = vt.split(Pattern.quote("|"));
			String cardNo = mt[0];
			String cvv = mt[1];
			cardReq.setSecretKey(card.getWayaPublicKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(cardNo);
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(cvv);
			cardReq.setCardHolder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
			log.info("Card Info: " + cardReq.toString());
		} else if (card.getScheme().equalsIgnoreCase("PayAttitude")) {
			cardReq.setSecretKey(card.getWayaPublicKey());
			cardReq.setScheme(card.getScheme());
			cardReq.setCardNumber(card.getEncryptCardNo());
			cardReq.setExpiry(card.getExpiry());
			cardReq.setCvv(card.getEncryptCardNo());
			cardReq.setCardHolder(card.getCardholder());
			cardReq.setMobile(card.getMobile());
			cardReq.setPin(card.getPin());
			log.info("Card Info: " + cardReq.toString());
		}
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Encrypt Card fail", null);
		String encryptData = uniPaymentProxy.encryptPaymentDataAccess(cardReq);
		if (!encryptData.isBlank()) {
			response = new PaymentGatewayResponse(true, "Success Encrypt", encryptData);
		}
		return response;
	}

	@Override
	public PaymentGatewayResponse CardAcquireCallback(HttpServletRequest request, HttpServletResponse response,
			WayaPaymentCallback pay) {
		PaymentGatewayResponse mResponse = new PaymentGatewayResponse(false, "Callback fail", null);
		try {
			PaymentGateway mPay = paymentGatewayRepo.findByTranId(pay.getTranId()).orElse(null);
			if (mPay != null) {
				mPay.setEncyptCard(pay.getCardEncrypt());
				paymentGatewayRepo.save(mPay);
				String callReq = uniPaymentProxy.getPaymentStatus(pay.getTranId(), pay.getCardEncrypt());
				if (!callReq.isBlank()) {
					// response.sendRedirect(callReq);
					URLConnection urlConnection_ = new URL(callReq).openConnection();
					urlConnection_.connect();
					BufferedInputStream bufferedInputStream = new BufferedInputStream(urlConnection_.getInputStream());
					String callbackResponse = new String(bufferedInputStream.readAllBytes());
					Jsoup.parse(callbackResponse).body().getElementsByTag("script").get(0);
					callbackResponse = callbackResponse.replace("\r", "").replace("\n", "").replace("\"", "")
							.replace("\\", "");
					UniPayment payment = new UniPayment(callbackResponse, callReq);
					mResponse = new PaymentGatewayResponse(true, "Success callback", payment);
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return new PaymentGatewayResponse(false, "Transaction Not Completed", null);
		}
		return mResponse;

	}

	@Override
	public PaymentGatewayResponse PayAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay) {
		PaymentGatewayResponse response = new PaymentGatewayResponse(false, "PayAttitude Callback fail", null);
		PaymentGateway mPay = paymentGatewayRepo.findByTranId(pay.getTranId()).orElse(null);
		if (mPay != null) {
			mPay.setEncyptCard(pay.getCardEncrypt());
			paymentGatewayRepo.save(mPay);
			WayaTransactionQuery callReq = uniPaymentProxy.postPayAttitude(pay);
			if (callReq != null) {
				response = new PaymentGatewayResponse(true, "Success Encrypt", callReq);
			}
		}
		return response;
	}

	@Override
	public ResponseEntity<?> GetTransactionStatus(HttpServletRequest req, String tranId) {
		WayaTransactionQuery response = null;
		/*
		 * String input = "NJOKU EMMANUEL IFEANYI"; final String secretKey =
		 * "ssshhhhhhhhhhh!!!!";
		 */
		try {
			/*
			 * String vt = UnifiedPaymentProxy.getDataEncrypt(input, secretKey);
			 * log.info(vt); log.info(UnifiedPaymentProxy.getDataDecrypt(vt, secretKey));
			 */
			response = uniPaymentProxy.transactionQuery(tranId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response == null) {
			return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
	}

	@Override
	public PaymentGatewayResponse encrypt(HttpServletRequest request, WayaEncypt pay) {
		String keygen = null;
		if (pay.getMerchantSecretKey().contains("TEST")) {
			keygen = pay.getMerchantSecretKey().replace("WAYAPUBK_TEST_0x", "");
			log.info(keygen);
		} else {
			keygen = pay.getMerchantSecretKey().replace("WAYAPUBK_PROD_0x", "");
			log.info(keygen);
		}
		String vt = UnifiedPaymentProxy.getDataEncrypt(pay.getEncryptString(), keygen);
		if (vt == null || vt.equals("")) {
			return (new PaymentGatewayResponse(false, "Encryption fail", null));
		}
		return (new PaymentGatewayResponse(true, "Encrypted", vt));
	}

	@Override
	public PaymentGatewayResponse decrypt(HttpServletRequest request, WayaDecypt pay) {
		String keygen = null;
		if (pay.getMerchantSecretKey().contains("TEST")) {
			keygen = pay.getMerchantSecretKey().replace("WAYAPUBK_TEST_0x", "");
			log.info(keygen);
		} else {
			keygen = pay.getMerchantSecretKey().replace("WAYAPUBK_PROD_0x", "");
			log.info(keygen);
		}
		String vt = UnifiedPaymentProxy.getDataDecrypt(pay.getDecryptString(), keygen);
		if (vt == null || vt.equals("")) {
			return (new PaymentGatewayResponse(false, "Decryption fail", null));
		}
		return (new PaymentGatewayResponse(true, "Decrypted", vt));
	}

}
