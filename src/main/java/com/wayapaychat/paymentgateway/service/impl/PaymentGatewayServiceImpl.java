package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.PaymentWallet;
import com.wayapaychat.paymentgateway.entity.RecurrentPayment;
import com.wayapaychat.paymentgateway.enumm.PaymentChannel;
import com.wayapaychat.paymentgateway.enumm.TStatus;
import com.wayapaychat.paymentgateway.enumm.TransactionSettled;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.pojo.*;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.ussd.USSDResponse;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.IdentityManager;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.PaymentWalletRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentPaymentRepository;
import com.wayapaychat.paymentgateway.service.GetUserDataService;
import com.wayapaychat.paymentgateway.service.MerchantProxy;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.service.UnifiedPaymentProxy;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final Integer DEFAULT_CARD_LENGTH = 20;
    private final Random rnd = new Random();
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private UnifiedPaymentProxy uniPaymentProxy;
    @Autowired
    private MerchantProxy merchantProxy;
    @Autowired
    private AuthApiClient authProxy;
    @Autowired
    private IdentityManager identManager;
    @Autowired
    private PaymentGatewayRepository paymentGatewayRepo;
    @Autowired
    private WalletProxy wallProxy;
    @Autowired
    private WayaPaymentDAO wayaPayment;
    @Autowired
    private PaymentWalletRepository paymentWalletRepo;
    @Autowired
    private RecurrentPaymentRepository recurrentPaymentRepository;
    @Value("${service.name}")
    private String username;
    @Value("${service.pass}")
    private String passSecret;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FraudEventImpl paymentGatewayFraudEvent;
    @Value("${service.encrypt-all-merchant-secretkey-with}")
    private String encryptAllMerchantSecretKeyWith;
    @Value("${service.wayapay-payment-status-url}")
    private String wayapayStatusURL;
    @Autowired
    private PaymentGateWayCommonUtils paymentGateWayCommonUtils;
    @Autowired
    private GetUserDataService getUserDataService;

    @Override
    public PaymentGatewayResponse initiateTransaction(HttpServletRequest request, WayaPaymentRequest transactionRequestPojo, Device device) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Unprocessed Transaction", null);
        try {
            LoginRequest auth = new LoginRequest();
            auth.setEmailOrPhoneNumber(username);
            auth.setPassword(passSecret);
            TokenAuthResponse authToken = authProxy.authenticateUser(auth);
            log.info("Response: " + authToken.toString());
            if (!authToken.getStatus()) {
                return new PaymentGatewayResponse(false, "Unable to authenticate Demon User", null);
            }
            PaymentData payData = authToken.getData();
            String token = payData.getToken();

            MerchantResponse merchant = null;
            try {
                merchant = merchantProxy.getMerchantInfo(token, transactionRequestPojo.getMerchantId());
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }
            if (merchant == null) {
                return new PaymentGatewayResponse(false, "Profile doesn't exist", null);
            }

            if (!merchant.getCode().equals("00")) {
                return new PaymentGatewayResponse(false, "Merchant id doesn't exist", null);
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();
            if (sMerchant.getMerchantKeyMode().equals("TEST")) {
                if (!transactionRequestPojo.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
                    return new PaymentGatewayResponse(false, "Invalid merchant key", null);
                }
            } else if (!transactionRequestPojo.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
                return new PaymentGatewayResponse(false, "Invalid merchant key", null);
            }
            // To create customer records
            CustomerRequest customer = new CustomerRequest();
            customer.setEmail(transactionRequestPojo.getCustomer().getEmail());
            customer.setMerchantPublicKey(transactionRequestPojo.getWayaPublicKey());
            customer.setPhoneNumber(transactionRequestPojo.getCustomer().getPhoneNumber());
            customer.setFirstName(transactionRequestPojo.getCustomer().getName());
            customer.setLastName(transactionRequestPojo.getCustomer().getName());
            MerchantCustomer merchantCustomer = identManager.postCustomerCreate(customer, token);
            log.info("CUSTOMER: " + merchantCustomer.toString());

            // Fetch Profile
            ProfileResponse profile = null;
            try {
                profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }
            if (profile == null) {
                return new PaymentGatewayResponse(false, "Profile doesn't exist", null);
            }

            PaymentGateway payment = new PaymentGateway();
            Date dte = new Date();
            long milliSeconds = dte.getTime();
            String strLong = Long.toString(milliSeconds);
            strLong = strLong + rnd.nextInt(999999);
            payment.setRefNo(strLong);
            payment.setMerchantId(transactionRequestPojo.getMerchantId());
            payment.setMerchantEmail(merchant.getData().getMerchantEmailAddress());
            payment.setDescription(transactionRequestPojo.getDescription());
            payment.setAmount(transactionRequestPojo.getAmount());
            payment.setFee(transactionRequestPojo.getFee());
            payment.setCustomerIpAddress(PaymentGateWayCommonUtils.getClientRequestIP(request));
            payment.setCurrencyCode(transactionRequestPojo.getCurrency());
            payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
            payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
            payment.setCustomerName(transactionRequestPojo.getCustomer().getName());
            payment.setCustomerEmail(transactionRequestPojo.getCustomer().getEmail());
            payment.setCustomerPhone(transactionRequestPojo.getCustomer().getPhoneNumber());
            payment.setStatus(TransactionStatus.PENDING);
            payment.setChannel(PaymentChannel.WEBVIEW);
            payment.setCustomerId(merchantCustomer.getData().getCustomerId());
            payment.setPaymentLinkId(transactionRequestPojo.getPaymentLinkId());
            payment.setPreferenceNo(transactionRequestPojo.getPreferenceNo());
            String encryptedMerchantSecretKey = UnifiedPaymentProxy.getDataEncrypt(transactionRequestPojo.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
            payment.setSecretKey(encryptedMerchantSecretKey);
            CardResponse card = new CardResponse();
            String tranId = UUID.randomUUID() + "";
            if (!tranId.isBlank()) {
                card.setTranId(strLong);
                card.setName(profile.getData().getOtherDetails().getOrganisationName());
                card.setCustomerId(merchantCustomer.getData().getCustomerId());
                card.setCustomerAvoid(merchantCustomer.getData().isCustomerAvoided());
                response = new PaymentGatewayResponse(true, "Success Transaction", card);
                payment.setTranId(tranId);
                payment.setTranDate(LocalDate.now());
                payment.setRcre_time(LocalDateTime.now());
                payment.setVendorDate(LocalDate.now());
                paymentGatewayRepo.save(payment);
            }
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private RecurrentPayment preprocessRecurrentPayment(UnifiedCardRequest cardRequest, WayaCardPayment card, PaymentGateway paymentGateway) {
        //TODO: UP For pay attitude, These fields are not present to tell when the
        // The recurring payment should happen
        // frequency , OrderExpirationPeriod
        @NotNull final String ORDER_TYPE = "Purchase";
        @NotNull final String DATE_SEPARATOR = "/";
        @NotNull final String PAY_ATTITUDE = "PayAttitude";
        PaymentLinkResponsePojo paymentLinkResponsePojo = identManager.getPaymentLinkDetailsById(card.getRecurrentPaymentDTO().getPaymentLinkId());
        RecurrentPayment recurrentPayment = null;
        cardRequest.setRecurring(true);
        if (paymentLinkResponsePojo.getLinkCanExpire() && ObjectUtils.isNotEmpty(paymentLinkResponsePojo.getExpiryDate())) {
            if (paymentLinkResponsePojo.getExpiryDate().isAfter(LocalDateTime.now())) {
                throw new ApplicationException(403, "01", "Payment link has expired and can't not be used to process payment");
            }
        } else {
            recurrentPayment = RecurrentPayment.
                    builder()
                    .active(false)
                    .paymentLinkId(paymentLinkResponsePojo.getPaymentLinkId())
                    .paymentLinkType(paymentLinkResponsePojo.getPaymentLinkType())
                    .intervalType(paymentLinkResponsePojo.getIntervalType())
                    .recurrentAmount(paymentLinkResponsePojo.getPayableAmount())
                    .nextChargeDate(LocalDateTime.now().plusDays(paymentLinkResponsePojo.getInterval()))
                    .customerId(paymentGateway.getCustomerId())
                    .totalCount(paymentLinkResponsePojo.getTotalCount())
                    .merchantId(paymentGateway.getMerchantId())
                    .planId(paymentLinkResponsePojo.getPlanId())
                    .startDateAfterFirstPayment(paymentLinkResponsePojo.getStartDateAfterFirstPayment())
                    .build();
            if (card.getScheme().equals(PAY_ATTITUDE)) {
                cardRequest.setCount(0);
                cardRequest.setOrderType(ORDER_TYPE);
            }
            if (ObjectUtils.isNotEmpty(paymentLinkResponsePojo.getStartDateAfterFirstPayment())) {
                cardRequest.setEndRecurr(LocalDateTime.now()
                        .plusDays(paymentLinkResponsePojo.getInterval())
                        .format(DateTimeFormatter.ISO_DATE)
                        .replace("-", DATE_SEPARATOR));
                cardRequest.setFrequency(paymentLinkResponsePojo.getTotalCount().toString());
                cardRequest.setOrderExpirationPeriod(paymentLinkResponsePojo.getInterval());
            } else {
                String endDateAfterFistPaymentIsMade = LocalDateTime.now()
                        .plusDays((long) paymentLinkResponsePojo.getInterval() * paymentLinkResponsePojo.getTotalCount())
                        .format(DateTimeFormatter.ISO_DATE)
                        .replace("-", DATE_SEPARATOR);
                cardRequest.setEndRecurr(endDateAfterFistPaymentIsMade);
                cardRequest.setFrequency(paymentLinkResponsePojo.getTotalCount().toString());
                cardRequest.setOrderExpirationPeriod(paymentLinkResponsePojo.getInterval());
            }
            recurrentPayment = recurrentPaymentRepository.save(recurrentPayment);
            paymentGateway.setRecurrentPaymentId(recurrentPayment.getId());
            paymentGateway.setIsFromRecurrentPayment(true);
        }
        return recurrentPayment;
    }

    @Override
    public ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, WayaCardPayment card) {
        UnifiedCardRequest upCardPaymentRequest = new UnifiedCardRequest();
        Optional<PaymentGateway> optionalPaymentGateway = paymentGatewayRepo.findByRefNo(card.getTranId());
        RecurrentPayment recurrentPayment;
        if (optionalPaymentGateway.isEmpty())
            return new ResponseEntity<>(new ErrorResponse("Transaction does not exists"), HttpStatus.BAD_REQUEST);
        PaymentGateway paymentGateway = optionalPaymentGateway.get();
        if (card.isRecurrentPayment()) {
            if (ObjectUtils.isEmpty(card.getRecurrentPaymentDTO().getPaymentLinkId()))
                throw new ApplicationException(400, "01", "Recurrent payment link Id is required");
            preprocessRecurrentPayment(upCardPaymentRequest, card, paymentGateway);
        }
        upCardPaymentRequest.setScheme(card.getScheme());
        upCardPaymentRequest.setExpiry(card.getExpiry());
        upCardPaymentRequest.setCardHolder(card.getCardholder());
        upCardPaymentRequest.setMobile(card.getMobile());
        upCardPaymentRequest.setPin(card.getPin());
        upCardPaymentRequest.setCardNumber(card.getEncryptCardNo());

        Object response;
        String pan = "**** **** **** ****";
        String keygen = replacePublicKeyWithEmptyString(card.getWayaPublicKey());

        if (card.getScheme().equalsIgnoreCase("Amex") || card.getScheme().equalsIgnoreCase("Mastercard")
                || card.getScheme().equalsIgnoreCase("Visa")) {
            String decryptedCard = UnifiedPaymentProxy.getDataDecrypt(card.getEncryptCardNo(), keygen);
            log.info(decryptedCard);
            if (ObjectUtils.isEmpty(decryptedCard)) {
                response = new PaymentGatewayResponse(false, "Invalid Encryption", null);
                new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (decryptedCard.length() < DEFAULT_CARD_LENGTH) {
                response = new PaymentGatewayResponse(false, "Invalid Card", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String[] mt = decryptedCard.split(Pattern.quote("|"));
            pan = mt[0];
            String cvv = mt[1];
            upCardPaymentRequest.setCardNumber(pan);
            upCardPaymentRequest.setCvv(cvv);
            log.info("Card Info: " + upCardPaymentRequest);
        } else if (card.getScheme().equalsIgnoreCase("Verve")) {
            String decryptedCardData = UnifiedPaymentProxy.getDataDecrypt(card.getEncryptCardNo(), keygen);
            log.info(decryptedCardData);
            if (ObjectUtils.isEmpty(decryptedCardData)) {
                response = new PaymentGatewayResponse(false, "Oops failed to process card", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (decryptedCardData.length() < DEFAULT_CARD_LENGTH) {
                response = new PaymentGatewayResponse(false, "Invalid Card", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (ObjectUtils.isEmpty(card.getCardholder()) || ObjectUtils.isEmpty(card.getExpiry())
                    || ObjectUtils.isEmpty(card.getPin()) || ObjectUtils.isEmpty(card.getMobile())) {
                response = new PaymentGatewayResponse(false, "Verve requires all fields to be provided", null);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String[] mt = decryptedCardData.split(Pattern.quote("|"));
            pan = mt[0];
            String cvv = mt[1];
            upCardPaymentRequest.setCardNumber(pan);
            upCardPaymentRequest.setCvv(cvv);
            log.info("Card Info: " + upCardPaymentRequest);
        } else if (card.getScheme().equalsIgnoreCase("PayAttitude")) {
            upCardPaymentRequest.setCvv(card.getEncryptCardNo());
            log.info("Card Info: " + upCardPaymentRequest);
        }
        response = new PaymentGatewayResponse(false, "Encrypt Card fail", null);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String encryptData = uniPaymentProxy.encryptPaymentDataAccess(upCardPaymentRequest);
        paymentGateway.setPaymentMetaData(card.getDeviceInformation());
        paymentGateway.setScheme(card.getScheme());
        paymentGateway.setMaskedPan(PaymentGateWayCommonUtils.maskedPan(pan));
        if (!encryptData.isBlank()) {
            response = new PaymentGatewayResponse(true, "Success Encrypt", encryptData);
            httpStatus = HttpStatus.OK;
        }
        paymentGatewayRepo.save(paymentGateway);
        return new ResponseEntity<>(response, httpStatus);
    }

    @Override
    public PaymentGatewayResponse processCardTransaction(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay) {
        PaymentGatewayResponse mResponse = new PaymentGatewayResponse(false, "Callback fail", null);
        try {
            PaymentGateway mPay = paymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
            if (mPay != null) {
                mPay.setEncyptCard(pay.getCardEncrypt());
                mPay.setChannel(PaymentChannel.CARD);
                WayaPaymentRequest mAccount = new WayaPaymentRequest(mPay.getMerchantId(), mPay.getDescription(),
                        mPay.getAmount(), mPay.getFee(), mPay.getCurrencyCode(), mPay.getSecretKey(),
                        new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone()),
                        mPay.getPreferenceNo());
                String tranId = uniPaymentProxy.postUnified(mAccount);
                if (ObjectUtils.isEmpty(tranId)) {
                    return new PaymentGatewayResponse(false, "Failed to process transaction authentication. Please try again later!", null);
                }
                mPay.setTranId(tranId);
                paymentGatewayRepo.save(mPay);
                String callReq = uniPaymentProxy.getPaymentStatus(tranId, pay.getCardEncrypt());
                if (!callReq.isBlank()) {
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
    public PaymentGatewayResponse payAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, "PayAttitude Callback fail", null);

        PaymentGateway mPay = paymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
        if (mPay != null) {
            mPay.setEncyptCard(pay.getCardEncrypt());
            mPay.setChannel(PaymentChannel.PAYATTITUDE);

            WayaPaymentRequest mAccount = new WayaPaymentRequest(mPay.getMerchantId(), mPay.getDescription(),
                    mPay.getAmount(), mPay.getFee(), mPay.getCurrencyCode(), mPay.getSecretKey(),
                    new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone()),
                    mPay.getPreferenceNo());
            String tranId = uniPaymentProxy.postUnified(mAccount);
            if (ObjectUtils.isEmpty(tranId)) {
                return new PaymentGatewayResponse(false, "Failed to process transaction authentication. Please try again later!", null);
            }
            mPay.setTranId(tranId);
            paymentGatewayRepo.save(mPay);

            WayaPayattitude attitude = new WayaPayattitude(tranId, pay.getCardEncrypt());
            WayaTransactionQuery callReq = uniPaymentProxy.postPayAttitude(attitude);
            if (callReq != null) {
                response = new PaymentGatewayResponse(true, "Success Encrypt", callReq);
            }
        }
        return response;
    }

    @Override
    public ResponseEntity<?> getTransactionStatus(HttpServletRequest req, String tranId) {
        WayaTransactionQuery response = null;
        try {
            response = uniPaymentProxy.transactionQuery(tranId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
    }

    @Override
    public WayaTransactionQuery getTransactionStatus(String tranId) {
        WayaTransactionQuery response = null;
        try {
            response = uniPaymentProxy.transactionQuery(tranId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public PaymentGatewayResponse encryptCard(HttpServletRequest request, WayaEncypt pay) {
        String keygen = replacePublicKeyWithEmptyString(pay.getMerchantSecretKey());
        String vt = UnifiedPaymentProxy.getDataEncrypt(pay.getEncryptString(), keygen);
        if (ObjectUtils.isEmpty(vt))
            return (new PaymentGatewayResponse(false, "Encryption fail", null));
        return (new PaymentGatewayResponse(true, "Encrypted", vt));
    }

    @Override
    public PaymentGatewayResponse decryptCard(HttpServletRequest request, WayaDecypt pay) {
        String keygen = replacePublicKeyWithEmptyString(pay.getMerchantSecretKey());
        String vt = UnifiedPaymentProxy.getDataDecrypt(pay.getDecryptString(), keygen);
        if (ObjectUtils.isEmpty(vt))
            return (new PaymentGatewayResponse(false, "Decryption fail", null));
        return (new PaymentGatewayResponse(true, "Decrypted", vt));
    }

    @Override
    public ResponseEntity<?> walletAuthentication(HttpServletRequest request, WayaAuthenicationRequest account) {
        try {
            LoginRequest auth = new LoginRequest();
            auth.setEmailOrPhoneNumber(account.getEmailOrPhoneNumber());
            auth.setPassword(account.getPassword());
            TokenAuthResponse authToken = authProxy.authenticateUser(auth);
            log.info("Response: " + authToken.toString());
            if (!authToken.getStatus()) {
                return new ResponseEntity<>(new ErrorResponse("AUTHENTICATION WALLET FAILED"), HttpStatus.BAD_REQUEST);
            }
            PaymentData payData = authToken.getData();
            String token = payData.getToken();
            User user = payData.getUser();

            WalletResponse wallet = wallProxy.getWalletDetails(token, user.getId());
            if (!wallet.getStatus()) {
                log.error("WALLET ERROR: " + wallet);
                return new ResponseEntity<>(new ErrorResponse(wallet.getMessage()), HttpStatus.BAD_REQUEST);
            }
            ProfileResponse profile = authProxy.getProfileDetail(user.getId(), token);

            WalletAuthResponse mWallet = new WalletAuthResponse();
            mWallet.setToken(token);
            mWallet.setWallet(wallet.getData());
            mWallet.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
            return new ResponseEntity<>(new SuccessResponse("WALLET PAYMENT", mWallet), HttpStatus.CREATED);
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Exception Occurred {}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> processWalletPayment(HttpServletRequest request, WayaWalletPayment account, String token) {
        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocessed Transaction Request"),
                HttpStatus.BAD_REQUEST);
        try {
            PaymentGateway payment = paymentGatewayRepo.findByRefNo(account.getRefNo()).orElse(null);
            if (payment == null) {
                return new ResponseEntity<>(new ErrorResponse("REFERENCE NUMBER DOESN'T EXIST"),
                        HttpStatus.BAD_REQUEST);
            }
            payment.setChannel(PaymentChannel.WALLET);
            paymentGatewayRepo.save(payment);

            if (payment.isSuccessfailure() && payment.getStatus().name().equals("TRANSACTION_COMPLETED")) {
                return new ResponseEntity<>(
                        new ErrorResponse("TRANSACTION ALREADY COMPLETED FOR REFERENCE NUMBER :" + payment.getRefNo()),
                        HttpStatus.BAD_REQUEST);
            }

            MerchantResponse merchant = merchantProxy.getMerchantInfo(token, payment.getMerchantId());
            if (!merchant.getCode().equals("00")) {
                return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();
            log.info("Merchant ID: " + sMerchant.getMerchantId());

            TokenCheckResponse auth = getUserDataService.getUserData(token);
            if (!auth.isStatus()) {
                return new ResponseEntity<>(new ErrorResponse("INVALID TOKEN"), HttpStatus.BAD_REQUEST);
            }
            MyUserData mAuth = auth.getData();

            try {
                PinResponse pin = authProxy.validatePin(mAuth.getId(), Long.valueOf(account.getPin()),
                        token);
                log.info("PIN RESPONSE: " + pin.toString());
                if (!pin.isStatus()) {
                    return new ResponseEntity<>(new ErrorResponse("INVALID PIN"), HttpStatus.BAD_REQUEST);
                }
            } catch (Exception ex) {
                log.info("PIN ERROR: " + ex.getLocalizedMessage());
                return new ResponseEntity<>(new ErrorResponse("TRANSACTION PIN NOT SETUP OR INVALID PIN"),
                        HttpStatus.OK);
            }
            PaymentWallet wallet = new PaymentWallet();
            FundEventResponse tran = uniPaymentProxy.postWalletTransaction(account, token, payment);
            if (tran != null) {
                response = new ResponseEntity<>(new SuccessResponse("SUCCESS TRANSACTION", tran.getTranId()),
                        HttpStatus.CREATED);
                payment.setTranId(tran.getTranId());
                payment.setTranDate(LocalDate.now());
                payment.setRcre_time(LocalDateTime.now());
                payment.setStatus(TransactionStatus.SUCCESSFUL);
                payment.setChannel(PaymentChannel.WALLET);
                payment.setSuccessfailure(true);
                paymentGatewayRepo.save(payment);

                wallet.setPaymentDescription(tran.getTranNarrate());
                wallet.setPaymentReference(tran.getPaymentReference());
                wallet.setTranAmount(tran.getTranAmount());
                wallet.setTranDate(tran.getTranDate());
                wallet.setTranId(tran.getTranId());
                wallet.setRefNo(payment.getRefNo());
                wallet.setSettled(TransactionSettled.NOT_SETTLED);
                wallet.setStatus(TStatus.APPROVED);
                paymentWalletRepo.save(wallet);
            } else {
                wallet.setPaymentDescription(payment.getDescription());
                wallet.setPaymentReference(payment.getPreferenceNo());
                wallet.setTranAmount(payment.getAmount());
                wallet.setStatus(TStatus.REJECTED);
                paymentWalletRepo.save(wallet);
            }
        } catch (Exception ex) {
            log.error("Error occurred - GET WALLET TRANSACTION :{}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @Override
    public ResponseEntity<?> walletPaymentQR(HttpServletRequest request, WayaQRRequest account) {
        ResponseEntity<?> response = new ResponseEntity<>(new ErrorResponse("Unprocessed Transaction Request"),
                HttpStatus.BAD_REQUEST);
        try {
            LoginRequest auth = new LoginRequest();
            auth.setEmailOrPhoneNumber(username);
            auth.setPassword(passSecret);
            TokenAuthResponse authToken = authProxy.authenticateUser(auth);
            log.info("Response: " + authToken.toString());
            if (!authToken.getStatus()) {
                return new ResponseEntity<>(new ErrorResponse("Unable to authenticate Demon User"),
                        HttpStatus.BAD_REQUEST);
            }
            PaymentData payData = authToken.getData();
            String token = payData.getToken();

            PaymentGateway payment = paymentGatewayRepo.findByRefNo(account.getRefNo()).orElse(null);
            if (payment == null) {
                return new ResponseEntity<>(new ErrorResponse("REFERENCE NUMBER DOESN'T EXIST"),
                        HttpStatus.BAD_REQUEST);
            }
            payment.setChannel(PaymentChannel.QR);
            payment.setStatus(TransactionStatus.PENDING);
            paymentGatewayRepo.save(payment);

            MerchantResponse merchant = merchantProxy.getMerchantInfo(token, payment.getMerchantId());
            if (!merchant.getCode().equals("00")) {
                return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();
            ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);
            payment.setChannel(PaymentChannel.QR);
            payment.setStatus(TransactionStatus.TRANSACTION_PENDING);

            WalletQRResponse tranRep = uniPaymentProxy.postQRTransaction(payment, token, account);
            if (tranRep != null) {
                tranRep.setName(profile.getData().getOtherDetails().getOrganisationName());
                response = new ResponseEntity<>(new SuccessResponse("SUCCESS GENERATED", tranRep), HttpStatus.CREATED);
                payment.setTranDate(LocalDate.now());
                payment.setRcre_time(LocalDateTime.now());
                paymentGatewayRepo.save(payment);
            }
        } catch (Exception ex) {
            log.error("Error occurred - GET QR TRANSACTION :{}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @Override
    public ResponseEntity<?> initiateWalletPayment(HttpServletRequest request, WayaWalletRequest account) {
        try {
            LoginRequest auth = new LoginRequest();
            auth.setEmailOrPhoneNumber(username);
            auth.setPassword(passSecret);
            TokenAuthResponse authToken = authProxy.authenticateUser(auth);
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
            log.info("Merchant: " + merchant);
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
            String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
            payment.setSecretKey(vt);
            payment.setTranId(account.getReferenceNo());
            payment.setPreferenceNo(account.getReferenceNo());
            payment.setTranDate(LocalDate.now());
            payment.setRcre_time(LocalDateTime.now());
            paymentGatewayRepo.save(payment);
            return new ResponseEntity<>(new SuccessResponse("SUCCESS WALLET", strLong), HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Error occurred - GET QR TRANSACTION :{}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> initiateUSSDTransaction(HttpServletRequest request, WayaUSSDRequest account) {
        try {
            LoginRequest auth = new LoginRequest();
            auth.setEmailOrPhoneNumber(username);
            auth.setPassword(passSecret);
            TokenAuthResponse authToken = authProxy.authenticateUser(auth);
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
            log.info("Merchant: " + merchant);
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
            String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
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
            payment.setVendorDate(LocalDate.now());
            PaymentGateway pay = paymentGatewayRepo.save(payment);
            USSDResponse ussd = new USSDResponse();
            ussd.setRefNo(pay.getRefNo());
            ussd.setName(profile.getData().getOtherDetails().getOrganisationName());
            return new ResponseEntity<>(new SuccessResponse("SUCCESS USSD", ussd), HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Error occurred - GET USSD TRANSACTION :{0}", ex);
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, WayaUSSDPayment account, String refNo) {
        //TODO: Query the transaction status again before updating the transaction
        PaymentGateway payment = paymentGatewayRepo.findByRefMerchant(refNo, account.getMerchantId()).orElse(null);
        if (payment == null) {
            return new ResponseEntity<>(new ErrorResponse("NO PAYMENT REQUEST INITIATED"), HttpStatus.BAD_REQUEST);
        }
        TransactionStatus status = TransactionStatus.valueOf(account.getStatus());
        payment.setStatus(status);
        payment.setTranId(account.getTranId());
        payment.setSuccessfailure(account.isSuccessfailure());
        LocalDate toDate = account.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        payment.setVendorDate(toDate);
        ReportPayment reportPayment = modelMapper.map(paymentGatewayRepo.save(payment), ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("TRANSACTION UPDATE", reportPayment), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> queryTranStatus(HttpServletRequest req) {
        @NotNull final String queryWithMerchantId = paymentGateWayCommonUtils.validateUserAndGetMerchantId(null);
        List<PaymentGateway> mPay = paymentGatewayRepo.findByPayment();
        if (mPay == null) {
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        }
        List<ReportPayment> sPay = mapList(mPay, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> getMerchantTransactionReport(HttpServletRequest req, String merchantId) {
        @NotNull final String queryWithMerchantId = paymentGateWayCommonUtils.validateUserAndGetMerchantId(merchantId);
        @NotNull final List<PaymentGateway> paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment(queryWithMerchantId);
        if (paymentGatewayList == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getTransactionByRef(HttpServletRequest req, String refNo) {
        WalletTransactionStatus response = new WalletTransactionStatus();
        PaymentGateway mPay = null;
        try {
            mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPay == null) {
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        }
        Customer customer = new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone());

        response = new WalletTransactionStatus(mPay.getPreferenceNo(), mPay.getAmount(), mPay.getDescription(),
                mPay.getFee(), mPay.getCurrencyCode(), mPay.getStatus().name(), mPay.getChannel().name(),
                mPay.getMerchantName(), customer);
        return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
    }

    //TODO: Protect this method to check is user has access to operate Payment gateway
    // PAYMENT_GATEWAY_TRANSACTION
    @Override
    public ResponseEntity<?> abandonTransaction(HttpServletRequest request, String refNo, WayaPaymentStatus pay) {
        PaymentGateway mPay = null;
        try {
            mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPay == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        mPay.setStatus(TransactionStatus.ABANDONED);
        paymentGatewayRepo.save(mPay);
        return new ResponseEntity<>(new SuccessResponse("Updated", "Success Updated"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest req, String merchantId) {
        @NotNull final String queryWithMerchantId = paymentGateWayCommonUtils.validateUserAndGetMerchantId(merchantId);
        WalletRevenue revenue = new WalletRevenue();
        try {
            revenue = wayaPayment.getRevenue(queryWithMerchantId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (revenue == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(new SuccessResponse("GET REVENUE", revenue), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest req) {
        if (!paymentGateWayCommonUtils.getAuthenticatedUser().getAdmin())
            throw new ApplicationException(403, "01", "Oops! Operation not allowed.");
        List<WalletRevenue> revenue = new ArrayList<>();
        try {
            revenue = wayaPayment.getRevenue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (revenue == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(new SuccessResponse("LIST REVENUE", revenue), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest requests) {
        PaymentGateway payment = paymentGatewayRepo.findByTranId(requests.getTrxId()).orElse(null);
        if (payment == null)
            return ResponseEntity.badRequest().body("UNKNOWN PAYMENT TRANSACTION STATUS");
        preprocessTransactionStatus(payment);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(wayapayStatusURL)).build();
    }

    @Override
    public ResponseEntity<?> updatePaymentStatus(String refNo) {
        PaymentGateway payment = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
        if (payment == null)
            return ResponseEntity.badRequest().body("UNKNOWN PAYMENT TRANSACTION STATUS");
        preprocessTransactionStatus(payment);
        return ResponseEntity.ok().body("Transaction status updated successful");
    }

    private void preprocessTransactionStatus(PaymentGateway payment) {
        WayaTransactionQuery response = uniPaymentProxy.transactionQuery(payment.getTranId());
        log.info("-----UNIFIED PAYMENT RESPONSE {}----------", response);
        if (ObjectUtils.isNotEmpty(response)) {
            if (ObjectUtils.isNotEmpty(response.getStatus()) && response.getStatus().toUpperCase().equals(TStatus.APPROVED.name())) {
                payment.setStatus(TransactionStatus.SUCCESSFUL);
                payment.setSuccessfailure(true);
                payment.setTranId(response.getOrderId());
            } else {
                TransactionStatus transactionStatus = Arrays.stream(TransactionStatus.values()).map(Enum::name)
                        .collect(Collectors.toList())
                        .contains(response.getStatus().toUpperCase()) ? TransactionStatus.valueOf(response.getStatus().toUpperCase()) : TransactionStatus.FAILED;
                payment.setStatus(transactionStatus);
                payment.setSuccessfailure(false);
                payment.setTranId(response.getOrderId());
            }
            paymentGatewayRepo.save(payment);
        }
    }

    private String replacePublicKeyWithEmptyString(String pub) {
        return pub.contains("WAYA") ?
                pub.replace("WAYAPUBK_TEST_0x", "") :
                pub.replace("WAYAPUBK_PROD_0x", "");
    }
}

