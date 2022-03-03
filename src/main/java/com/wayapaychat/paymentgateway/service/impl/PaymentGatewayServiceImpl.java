package com.wayapaychat.paymentgateway.service.impl;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.MerchantData;
import com.wayapaychat.paymentgateway.pojo.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.PaymentData;
import com.wayapaychat.paymentgateway.pojo.PaymentGatewayResponse;
import com.wayapaychat.paymentgateway.pojo.ProfileResponse;
import com.wayapaychat.paymentgateway.pojo.ReportPayment;
import com.wayapaychat.paymentgateway.pojo.SuccessResponse;
import com.wayapaychat.paymentgateway.pojo.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.User;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.CardResponse;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UniPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedCardRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaCardPayment;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaDecypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaEncypt;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentCallback;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaPaymentRequest;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.WayaTransactionQuery;
import com.wayapaychat.paymentgateway.pojo.ussd.USSDResponse;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.FundEventResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WalletAuthResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WalletQRResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WalletResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WayaAuthenicationRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaQRRequest;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WayaWalletRequest;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.service.MerchantProxy;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.UnifiedPaymentProxy;

import feign.FeignException;
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

	@Autowired
	WalletProxy wallProxy;

	@Value("${service.name}")
	private String username;

	@Value("${service.pass}")
	private String passSecret;
	
	ModelMapper modelMapper = new ModelMapper();

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
			if (!authToken.getStatus()) {
				return new PaymentGatewayResponse(false, "Unable to authenticate Demon User", null);
			}
			PaymentData payData = authToken.getData();
			String token = payData.getToken();
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
			// Fetch Profile
			ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);

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
			payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
			payment.setCustomerName(account.getCustomer().getName());
			payment.setCustomerEmail(account.getCustomer().getEmail());
			payment.setCustomerPhone(account.getCustomer().getPhoneNumber());
			payment.setStatus(TransactionStatus.TRANSACTION_PENDING);
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
			payment.setSecretKey(vt);
			CardResponse card = new CardResponse();
			String tranId = uniPaymentProxy.postUnified(account);
			if (!tranId.isBlank()) {
				card.setTranId(tranId);
				card.setName(profile.getData().getOtherDetails().getOrganisationName());
				response = new PaymentGatewayResponse(true, "Success Transaction", card);
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
				mPay.setChannel(PaymentChannel.CARD);
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
			mPay.setChannel(PaymentChannel.PAYATTITUDE);
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

	@Override
	public ResponseEntity<?> WalletPaymentAuthentication(HttpServletRequest request, WayaAuthenicationRequest account) {
		ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocess Transaction Request"),
				HttpStatus.BAD_REQUEST);
		try {
			LoginRequest auth = new LoginRequest();
			auth.setEmailOrPhoneNumber(account.getEmailOrPhoneNumber());
			auth.setPassword(account.getPassword());
			TokenAuthResponse authToken = authProxy.UserLogin(auth);
			log.info("Response: " + authToken.toString());
			if (!authToken.getStatus()) {
				return new ResponseEntity<>(new ErrorResponse("AUTHENTICATION WALLET FAILED"), HttpStatus.BAD_REQUEST);
			}
			PaymentData payData = authToken.getData();
			String token = payData.getToken();
			User user = payData.getUser();

			WalletResponse wallet = wallProxy.getWalletDetails(token, user.getId());
			if (!wallet.getStatus()) {
				log.error("WALLET ERROR: " + wallet.toString());
				return new ResponseEntity<>(new ErrorResponse(wallet.getMessage()), HttpStatus.BAD_REQUEST);
			}
			WalletAuthResponse mWallet = new WalletAuthResponse();
			mWallet.setToken(token);
			mWallet.setWallet(wallet.getData());
			// Payment Request
			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setCurrencyCode("NGN");
			payment.setMerchantId("WALLET PAYMENT");
			payment.setReturnUrl("http://localhost");
			payment.setSecretKey("PUBLIC KEY");
			String tempTranId = wallet.getTimeStamp() + strLong;
			payment.setTranId(tempTranId);
			payment.setTranDate(LocalDate.now());
			payment.setRcre_time(LocalDateTime.now());
			mWallet.setRefNo(strLong);
			paymentGatewayRepo.save(payment);

			response = new ResponseEntity<>(new SuccessResponse("WALLET PAYMENT", mWallet), HttpStatus.CREATED);

		} catch (Exception ex) {
			if (ex instanceof FeignException) {
				String httpStatus = Integer.toString(((FeignException) ex).status());
				log.error("Feign Exception Status {}", httpStatus);
			}
			log.error("Higher Wahala {}", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> ConsumeWalletPayment(HttpServletRequest request, WayaWalletPayment account, String token) {
		ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocess Transaction Request"),
				HttpStatus.BAD_REQUEST);
		try {
			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getMerchantId());
			if (!merchant.getCode().equals("00")) {
				return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
			}
			log.info("Merchant: " + merchant.toString());
			MerchantData sMerchant = merchant.getData();
			if (sMerchant.getMerchantKeyMode().equals("TEST")) {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			} else {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			}
			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getMerchantId());
			payment.setDescription(account.getPaymentDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getCurrency());
			payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
			payment.setSecretKey(vt);

			FundEventResponse tran = uniPaymentProxy.postWalletTransaction(account, token, strLong);
			if (tran != null) {
				response = new ResponseEntity<>(new SuccessResponse("SUCCESS TRANSACTION", tran.getTranId()),
						HttpStatus.CREATED);
				payment.setTranId(tran.getTranId());
				payment.setTranDate(LocalDate.now());
				payment.setRcre_time(LocalDateTime.now());
				paymentGatewayRepo.save(payment);
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET WALLET TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> WalletPaymentQR(HttpServletRequest request, WayaQRRequest account) {
		ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocess Transaction Request"),
				HttpStatus.BAD_REQUEST);
		try {
			LoginRequest auth = new LoginRequest();
			auth.setEmailOrPhoneNumber(username);
			auth.setPassword(passSecret);
			TokenAuthResponse authToken = authProxy.UserLogin(auth);
			log.info("Response: " + authToken.toString());
			if (!authToken.getStatus()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to authenticate Demon User"),
						HttpStatus.BAD_REQUEST);
			}
			PaymentData payData = authToken.getData();
			String token = payData.getToken();

			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getMerchantId());
			if (!merchant.getCode().equals("00")) {
				return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
			}
			log.info("Merchant: " + merchant.toString());
			MerchantData sMerchant = merchant.getData();
			if (sMerchant.getMerchantKeyMode().equals("TEST")) {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			} else {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			}
			// Fetch Profile
			ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);

			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getMerchantId());
			payment.setDescription(account.getPaymentDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getCurrency());
			payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
			payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
			payment.setCustomerName(account.getCustomer().getName());
			payment.setCustomerEmail(account.getCustomer().getEmail());
			payment.setChannel(PaymentChannel.QR);
			payment.setStatus(TransactionStatus.TRANSACTION_PENDING);
			payment.setCustomerPhone(account.getCustomer().getPhoneNumber());
			
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
			payment.setSecretKey(vt);

			WalletQRResponse tranRep = uniPaymentProxy.postQRTransaction(account, token);
			if (tranRep != null) {
				tranRep.setName(profile.getData().getOtherDetails().getOrganisationName());
				response = new ResponseEntity<>(new SuccessResponse("SUCCESS QR", tranRep), HttpStatus.CREATED);
				payment.setTranId(account.getReferenceNo());
				payment.setPreferenceNo(account.getReferenceNo());
				payment.setTranDate(LocalDate.now());
				payment.setRcre_time(LocalDateTime.now());
				paymentGatewayRepo.save(payment);
			}
		} catch (Exception ex) {
			log.error("Error occurred - GET QR TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> PostWalletPayment(HttpServletRequest request, WayaWalletRequest account) {
		ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocess Transaction Request"),
				HttpStatus.BAD_REQUEST);
		try {
			LoginRequest auth = new LoginRequest();
			auth.setEmailOrPhoneNumber(username);
			auth.setPassword(passSecret);
			TokenAuthResponse authToken = authProxy.UserLogin(auth);
			log.info("Response: " + authToken.toString());
			if (!authToken.getStatus()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to authenticate Demon User"),
						HttpStatus.BAD_REQUEST);
			}
			PaymentData payData = authToken.getData();
			String token = payData.getToken();

			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getMerchantId());
			if (!merchant.getCode().equals("00")) {
				return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
			}
			log.info("Merchant: " + merchant.toString());
			MerchantData sMerchant = merchant.getData();
			if (sMerchant.getMerchantKeyMode().equals("TEST")) {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			} else {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			}
			// Fetch Profile
			ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);

			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getMerchantId());
			payment.setDescription(account.getPaymentDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getCurrency());
			payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
			payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
			payment.setCustomerName(account.getCustomer().getName());
			payment.setCustomerEmail(account.getCustomer().getEmail());
			payment.setCustomerPhone(account.getCustomer().getPhoneNumber());
			payment.setChannel(PaymentChannel.WALLET);
			payment.setStatus(TransactionStatus.TRANSACTION_PENDING);
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
			payment.setSecretKey(vt);
			response = new ResponseEntity<>(new SuccessResponse("SUCCESS WALLET", strLong), HttpStatus.CREATED);
			payment.setTranId(account.getReferenceNo());
			payment.setPreferenceNo(account.getReferenceNo());
			payment.setTranDate(LocalDate.now());
			payment.setRcre_time(LocalDateTime.now());
			paymentGatewayRepo.save(payment);

		} catch (

		Exception ex) {
			log.error("Error occurred - GET QR TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> USSDPaymentRequest(HttpServletRequest request, WayaUSSDRequest account) {
		ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocess Transaction Request"),
				HttpStatus.BAD_REQUEST);
		try {
			LoginRequest auth = new LoginRequest();
			auth.setEmailOrPhoneNumber(username);
			auth.setPassword(passSecret);
			TokenAuthResponse authToken = authProxy.UserLogin(auth);
			log.info("Response: " + authToken.toString());
			if (!authToken.getStatus()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to authenticate Demon User"),
						HttpStatus.BAD_REQUEST);
			}
			PaymentData payData = authToken.getData();
			String token = payData.getToken();
			MerchantResponse merchant = merchantProxy.getMerchantInfo(token, account.getMerchantId());
			if (!merchant.getCode().equals("00")) {
				return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
			}
			log.info("Merchant: " + merchant.toString());
			MerchantData sMerchant = merchant.getData();
			if (sMerchant.getMerchantKeyMode().equals("TEST")) {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			} else {
				if (!account.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
					return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
				}
			}
			// Fetch Profile
			ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);

			PaymentGateway payment = new PaymentGateway();
			Date dte = new Date();
			long milliSeconds = dte.getTime();
			String strLong = Long.toString(milliSeconds);
			payment.setRefNo(strLong);
			payment.setMerchantId(account.getMerchantId());
			payment.setDescription(account.getPaymentDescription());
			payment.setAmount(account.getAmount());
			payment.setFee(account.getFee());
			payment.setCurrencyCode(account.getCurrency());
			payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
			final String secretKey = "ssshhhhhhhhhhh!!!!";
			String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), secretKey);
			payment.setSecretKey(vt);
			payment.setTranId(account.getReferenceNo());
			payment.setPreferenceNo(account.getReferenceNo());
			payment.setTranDate(LocalDate.now());
			payment.setRcre_time(LocalDateTime.now());
			payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
			payment.setCustomerName(account.getCustomer().getName());
			payment.setCustomerEmail(account.getCustomer().getEmail());
			payment.setCustomerPhone(account.getCustomer().getPhoneNumber());
			payment.setChannel(PaymentChannel.USSD);
			payment.setStatus(TransactionStatus.TRANSACTION_PENDING);
			PaymentGateway pay = paymentGatewayRepo.save(payment);
			USSDResponse ussd = new USSDResponse();
			ussd.setRefNo(pay.getRefNo());
			ussd.setName(profile.getData().getOtherDetails().getOrganisationName());
			response = new ResponseEntity<>(new SuccessResponse("SUCCESS USSD", ussd), HttpStatus.CREATED);

		} catch (Exception ex) {
			log.error("Error occurred - GET USSD TRANSACTION :", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public ResponseEntity<?> USSDPayment(HttpServletRequest request, WayaUSSDPayment account, String refNo) {
		PaymentGateway payment = paymentGatewayRepo.findByRefMerchant(refNo, account.getMerchantId()).orElse(null);
		if (payment == null) {
			return new ResponseEntity<>(new ErrorResponse("NO PAYMENT REQUEST INITIATED"), HttpStatus.BAD_REQUEST);
		}
		TransactionStatus channel = TransactionStatus.valueOf(account.getStatus());
		payment.setStatus(channel);
		payment.setTranId(account.getTranId());
		payment.setSuccessfailure(account.isSuccessfailure());
		LocalDate toDate = account.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		payment.setVendorDate(toDate);
		PaymentGateway mPayment = paymentGatewayRepo.save(payment);
		return new ResponseEntity<>(new SuccessResponse("TRANSACTION UPDATE", mPayment), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> USSDWalletPayment(HttpServletRequest request,
			com.wayapaychat.paymentgateway.pojo.ussd.USSDWalletPayment account) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> QueryTranStatus(HttpServletRequest req) {
		List<PaymentGateway> mPay = paymentGatewayRepo.findByPayment();
		if (mPay == null) {
			return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
		}
		List<ReportPayment> sPay = mapList(mPay, ReportPayment.class);
		return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
	}
	
	<S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
	    return source
	      .stream()
	      .map(element -> modelMapper.map(element, targetClass))
	      .collect(Collectors.toList());
	}

	@Override
	public ResponseEntity<?> QueryMerchantTranStatus(HttpServletRequest req, String merchantId) {
		List<PaymentGateway> mPay = paymentGatewayRepo.findByMerchantPayment(merchantId);
		if (mPay == null) {
			return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
		}
		List<ReportPayment> sPay = mapList(mPay, ReportPayment.class);
		return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
	}

}
