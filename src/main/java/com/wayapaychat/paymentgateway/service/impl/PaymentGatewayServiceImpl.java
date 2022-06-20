package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.common.enums.PaymentLinkType;
import com.wayapaychat.paymentgateway.common.enums.ProductName;
import com.wayapaychat.paymentgateway.common.enums.RecurrentPaymentStatus;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAOImpl;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.entity.PaymentWallet;
import com.wayapaychat.paymentgateway.entity.RecurrentTransaction;
import com.wayapaychat.paymentgateway.entity.listener.PaymemtGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.*;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.kafkamessagebroker.producer.IkafkaMessageProducer;
import com.wayapaychat.paymentgateway.pojo.User;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.*;
import com.wayapaychat.paymentgateway.pojo.ussd.USSDResponse;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDPayment;
import com.wayapaychat.paymentgateway.pojo.ussd.WayaUSSDRequest;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.*;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.event.SubscriptionEventPayload;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionOverviewResponse;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionRevenueStats;
import com.wayapaychat.paymentgateway.pojo.waya.stats.TransactionYearMonthStats;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.*;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.ISettlementProductPricingProxy;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;
import com.wayapaychat.paymentgateway.proxy.WalletProxy;
import com.wayapaychat.paymentgateway.proxy.pojo.MerchantProductPricingQuery;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import com.wayapaychat.paymentgateway.repository.PaymentWalletRepository;
import com.wayapaychat.paymentgateway.repository.RecurrentTransactionRepository;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils.getMerchantIdToUse;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final Integer DEFAULT_CARD_LENGTH = 20;
    private final Random rnd = new Random();
    private final ModelMapper modelMapper = new ModelMapper();
    private final String DEFAULT_SUCCESS_MESSAGE = "Data fetched successfully";
    @Autowired
    private UnifiedPaymentProxy uniPaymentProxy;
    @Autowired
    private MerchantProxy merchantProxy;
    @Autowired
    private AuthApiClient authProxy;
    @Autowired
    private IdentityManagementServiceProxy identManager;
    @Autowired
    private ISettlementProductPricingProxy iSettlementProductPricingProxy;
    @Autowired
    private PaymentGatewayRepository paymentGatewayRepo;
    @Autowired
    private WalletProxy wallProxy;
    @Autowired
    private WayaPaymentDAO wayaPayment;
    @Autowired
    private PaymentWalletRepository paymentWalletRepo;
    @Autowired
    private RecurrentTransactionRepository recurrentTransactionRepository;
    @Value("${service.name}")
    private String username;
    @Value("${service.pass}")
    private String passSecret;
    @Value("${service.token}")
    private String daemonToken;
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
    @Autowired
    private WayaPaymentDAOImpl wayaPaymentDAO;
    @Autowired
    private PaymemtGatewayEntityListener paymemtGatewayEntityListener;
    @Autowired
    private IkafkaMessageProducer messageQueueProducer;

    @Override
    public PaymentGatewayResponse initiateCardTransaction(HttpServletRequest request, WayaPaymentRequest transactionRequestPojo, Device device) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Unprocessed Transaction", null);
        try {
            MerchantResponse merchant = null;
            String token = paymentGateWayCommonUtils.getDaemonAuthToken();
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
            String[] customerName = transactionRequestPojo.getCustomer().getName().split("\\s+");
            CustomerRequest customer = new CustomerRequest();
            customer.setEmail(transactionRequestPojo.getCustomer().getEmail());
            customer.setMerchantPublicKey(transactionRequestPojo.getWayaPublicKey());
            customer.setPhoneNumber(transactionRequestPojo.getCustomer().getPhoneNumber());
            customer.setFirstName(ObjectUtils.isEmpty(customerName[0]) ? " " : customerName[0]);
            customer.setLastName(ObjectUtils.isEmpty(customerName[1]) ? " " : customerName[1]);
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
            //TODO: update wayapay processing fee... update the region later
            // get the IP region of where the transaction was initiated from
            BigDecimal wayapayFee = calculateWayapayFee(
                    sMerchant.getMerchantId(), transactionRequestPojo.getAmount(),
                    ProductName.CARD, "LOCAL", token);
            payment.setWayapayFee(wayapayFee);
            payment.setCustomerIpAddress(PaymentGateWayCommonUtils.getClientRequestIP(request));
            payment.setCurrencyCode(transactionRequestPojo.getCurrency());
            payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
            payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
            payment.setCustomerName(transactionRequestPojo.getCustomer().getName());
            payment.setCustomerEmail(transactionRequestPojo.getCustomer().getEmail());
            payment.setCustomerPhone(transactionRequestPojo.getCustomer().getPhoneNumber());
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.PENDING);
            payment.setChannel(PaymentChannel.CARD);
            payment.setCustomerId(merchantCustomer.getData().getCustomerId());
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
                payment.setTranDate(LocalDateTime.now());
                payment.setRcre_time(LocalDateTime.now());
                payment.setVendorDate(LocalDateTime.now());
                paymentGatewayRepo.save(payment);
            }
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    @Override
    public void preprocessRecurrentPayment(UnifiedCardRequest cardRequest, WayaCardPayment card, PaymentGateway paymentGateway) {
        //TODO: UP For pay attitude, These fields are not present to tell when the
        // The recurring payment should happen
        // frequency , OrderExpirationPeriod
        @NotNull final String ORDER_TYPE = "Purchase";
        @NotNull final String DATE_SEPARATOR = "/";
        @NotNull final String PAY_ATTITUDE = "PayAttitude";
        PaymentLinkResponse paymentLinkResponse = identManager.getPaymentLinkDetailsById(paymentGateWayCommonUtils.getDaemonAuthToken(), card.getPaymentLinkId()).getData();
        cardRequest.setRecurring(true);

        if (paymentLinkResponse.getPaymentLinkType() == PaymentLinkType.ONE_TIME_PAYMENT_LINK) {
            throw new ApplicationException(403, "01", "One time payment link can't be used for recurrent payment");
        } else if (paymentLinkResponse.getIntervalType() == null) {
            throw new ApplicationException(403, "01", "Payment link does not have interval type. " +
                    "Kindly provide one with recurrent interval to charge customer");
        } else if (paymentLinkResponse.getPaymentLinkType() == PaymentLinkType.CUSTOMER_SUBSCRIPTION_PAYMENT_LINK &&
                !Objects.equals(paymentGateway.getCustomerId(), paymentLinkResponse.getBelongsToCustomerId())) {
            throw new ApplicationException(403, "01", String.format("Payment link does not belong to this customer %s", paymentGateway.getCustomerId()) +
                    "Kindly provide one with recurrent interval to charge customer");
        } else if (paymentLinkResponse.getLinkCanExpire() && ObjectUtils.isNotEmpty(paymentLinkResponse.getExpiryDate())) {
            if (paymentLinkResponse.getExpiryDate().isAfter(LocalDateTime.now())) {
                throw new ApplicationException(403, "01", "Payment link has expired and can't not be used to process payment");
            }
        }

        Optional<RecurrentTransaction> optionalRecurrentPayment = recurrentTransactionRepository.getByTransactionRef(paymentGateway.getRefNo());
        RecurrentTransaction recurrentTransaction = null;
        if (optionalRecurrentPayment.isPresent()) {
            recurrentTransaction = optionalRecurrentPayment.get();
            if (recurrentTransaction.getActive())
                throw new ApplicationException(403, "01", "Recurrent payment still active. Payment can't be processed");
            if (ObjectUtils.isNotEmpty(recurrentTransaction.getNextChargeDate()) && recurrentTransaction.getNextChargeDate().isBefore(LocalDateTime.now()))
                throw new ApplicationException(403, "01", "Recurrent payment has not yet expired.");
            else {
                recurrentTransaction.setCurrentTransactionRefNo(paymentGateway.getRefNo());
                recurrentTransaction.setDateModified(LocalDateTime.now());
                recurrentTransaction.setModifiedBy(0L);
                preprocessCardRequest(paymentLinkResponse, cardRequest);
                return;
            }
        }

        recurrentTransaction = RecurrentTransaction.
                builder()
                .active(false)
                .paymentLinkId(paymentLinkResponse.getPaymentLinkId())
                .paymentLinkType(paymentLinkResponse.getPaymentLinkType())
                .intervalType(paymentLinkResponse.getIntervalType())
                .interval(paymentLinkResponse.getInterval())
                .recurrentAmount(paymentLinkResponse.getPayableAmount())
                .nextChargeDate(LocalDateTime.now().plusDays(paymentLinkResponse.getInterval()))
                .customerId(paymentGateway.getCustomerId())
                .customerSubscriptionId(paymentLinkResponse.getCustomerSubscriptionId())
                .maxChargeCount(paymentLinkResponse.getTotalCount())
                .merchantId(paymentGateway.getMerchantId())
                .currentTransactionRefNo(paymentGateway.getRefNo())
                .planId(paymentLinkResponse.getPlanId())
                .nextChargeDateAfterFirstPayment(paymentLinkResponse.getStartDateAfterFirstPayment())
                .build();

        if (card.getScheme().equals(PAY_ATTITUDE)) {
//            cardRequest.setCount(0);
//            cardRequest.setOrderType(ORDER_TYPE);
        }
        preprocessCardRequest(paymentLinkResponse, cardRequest);
        recurrentTransaction = recurrentTransactionRepository.save(recurrentTransaction);
        paymentGateway.setRecurrentPaymentId(recurrentTransaction.getId());
        paymentGateway.setPaymentLinkId(recurrentTransaction.getPaymentLinkId());
        paymentGateway.setIsFromRecurrentPayment(true);
    }

    private void preprocessCardRequest(PaymentLinkResponse paymentLinkResponse, UnifiedCardRequest cardRequest) {
        if (ObjectUtils.isNotEmpty(paymentLinkResponse.getStartDateAfterFirstPayment())) {
            cardRequest.setEndRecurr(LocalDateTime.now()
                    .plusDays(paymentLinkResponse.getInterval())
                    .format(DateTimeFormatter.ISO_DATE)
                    .replace("-", "/"));
            cardRequest.setFrequency(paymentLinkResponse.getTotalCount().toString());
            cardRequest.setOrderExpirationPeriod(paymentLinkResponse.getInterval());
        } else {
            String endDateAfterFistPaymentIsMade = LocalDateTime.now()
                    .plusDays((long) paymentLinkResponse.getInterval() * paymentLinkResponse.getTotalCount())
                    .format(DateTimeFormatter.ISO_DATE)
                    .replace("-", "/");
            cardRequest.setEndRecurr(endDateAfterFistPaymentIsMade);
            cardRequest.setFrequency(paymentLinkResponse.getTotalCount().toString());
            cardRequest.setOrderExpirationPeriod(paymentLinkResponse.getInterval());
        }
    }

    @Override
    public ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, WayaCardPayment card) throws JsonProcessingException {
        UnifiedCardRequest upCardPaymentRequest = new UnifiedCardRequest();
        Optional<PaymentGateway> optionalPaymentGateway = paymentGatewayRepo.findByRefNo(card.getTranId());
        RecurrentTransaction recurrentTransaction;
        if (optionalPaymentGateway.isEmpty())
            return new ResponseEntity<>(new ErrorResponse("Transaction does not exists"), HttpStatus.BAD_REQUEST);
        PaymentGateway paymentGateway = optionalPaymentGateway.get();
        if (paymentGateway.getTransactionExpired()) {
            if (!(paymentGateway.getStatus() == TransactionStatus.ABANDONED)) {
                paymentGateway.setStatus(TransactionStatus.ABANDONED);
                CompletableFuture.runAsync(() -> paymentGatewayRepo.save(paymentGateway));
            }
            throw new ApplicationException(400, "01", String.format("Oops! Transaction with transaction reference %s has expired!", paymentGateway.getRefNo()));
        } else if (card.isRecurrentPayment()) {
            if (ObjectUtils.isEmpty(card.getPaymentLinkId()))
                throw new ApplicationException(400, "01", "Recurrent payment link Id is required");
            preprocessRecurrentPayment(upCardPaymentRequest, card, paymentGateway);
        } else if (paymentGateway.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL)
            return new ResponseEntity<>(new ErrorResponse("Transaction already successful"), HttpStatus.FORBIDDEN);
        upCardPaymentRequest.setScheme(card.getScheme());
        upCardPaymentRequest.setExpiry(card.getExpiry());
        upCardPaymentRequest.setCardHolder(card.getCardholder());
        upCardPaymentRequest.setMobile(card.getMobile());
        upCardPaymentRequest.setPin(card.getPin());
        upCardPaymentRequest.setCardNumber(card.getEncryptCardNo());

        Object response;
        String pan = "**** **** **** ****";
        String keygen = replaceKeyPrefixWithEmptyString(card.getWayaPublicKey());

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
            if (mt.length < 2)
                throw new ApplicationException(400, "01", "Card missing all correct fields. Ensure card is encrypted properly.");
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
            if (mt.length < 2)
                throw new ApplicationException(400, "01", "Card missing all correct fields. Ensure card is encrypted properly.");
            pan = mt[0];
            String cvv = mt[1];
            upCardPaymentRequest.setCardNumber(pan);
            upCardPaymentRequest.setCvv(cvv);
            log.info("Card Info: " + upCardPaymentRequest);
        } else if (card.getScheme().equalsIgnoreCase("PayAttitude")) {
            //TODO: Get the country IP Address to be able to charge the customer with fee
            upCardPaymentRequest.setCvv(card.getEncryptCardNo());
            paymentGateway.setChannel(PaymentChannel.PAYATTITUDE);
            BigDecimal wayapayFee = calculateWayapayFee(
                    paymentGateway.getMerchantId(), paymentGateway.getAmount(),
                    ProductName.PAYATTITUDE, "LOCAL", daemonToken);
            paymentGateway.setWayapayFee(wayapayFee);
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
    public PaymentGatewayResponse processCardTransaction(HttpServletRequest request, HttpServletResponse
            response, WayaPaymentCallback pay) {
        PaymentGatewayResponse mResponse = new PaymentGatewayResponse(false, "Callback fail", null);
        try {
            PaymentGateway mPay = paymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
            if (mPay != null) {
                mPay.setEncyptCard(pay.getCardEncrypt());
                mPay.setChannel(PaymentChannel.CARD);
                WayaPaymentRequest mAccount = new WayaPaymentRequest(mPay.getMerchantId(), mPay.getDescription(),
                        mPay.getAmount(), mPay.getFee(), mPay.getCurrencyCode(), mPay.getSecretKey(),
                        new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId()),
                        mPay.getPreferenceNo());
                String tranId = uniPaymentProxy.postUnified(mAccount);
                if (ObjectUtils.isEmpty(tranId))
                    return new PaymentGatewayResponse(false, "Failed to initiate post tranId for 3D Authentication.", null);
                mPay.setTranId(tranId);
                paymentGatewayRepo.save(mPay);
                String callReq = uniPaymentProxy.buildUnifiedPaymentURLWithPayload(tranId, pay.getCardEncrypt(), false);
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
            log.error("----|||| ERROR OCCURRED {0} ||||----", ex);
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
                    new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId()),
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
        String keygen = replaceKeyPrefixWithEmptyString(pay.getMerchantPublicKey());
        String vt = UnifiedPaymentProxy.getDataEncrypt(pay.getEncryptString(), keygen);
        if (ObjectUtils.isEmpty(vt))
            return (new PaymentGatewayResponse(false, "Encryption fail", null));
        return (new PaymentGatewayResponse(true, "Encrypted", vt));
    }

    @Override
    public PaymentGatewayResponse decryptCard(HttpServletRequest request, WayaDecypt pay) {
        String keygen = replaceKeyPrefixWithEmptyString(pay.getMerchantPublicKey());
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
        PaymentGateway payment;
        try {
            payment = paymentGatewayRepo.findByRefNo(account.getRefNo()).orElse(null);
            if (payment == null) {
                return new ResponseEntity<>(new ErrorResponse("REFERENCE NUMBER DOESN'T EXIST"),
                        HttpStatus.BAD_REQUEST);
            }
            payment.setChannel(PaymentChannel.WALLET);
            paymentGatewayRepo.save(payment);

            if (payment.isSuccessfailure() && payment.getStatus().name().equals("SUCCESSFUL")) {
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
            AuthenticatedUser mAuth = auth.getData();

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
                payment.setTranDate(LocalDateTime.now());
                payment.setRcre_time(LocalDateTime.now());
                payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL);
                payment.setChannel(PaymentChannel.WALLET);
                //TODO: update wayapay processing fee... update the region later
                // get the IP region of where the transaction was initiated from
                BigDecimal wayapayFee = calculateWayapayFee(
                        sMerchant.getMerchantId(), payment.getAmount(),
                        ProductName.WALLET, "LOCAL", token);
                payment.setWayapayFee(wayapayFee);

                payment.setPaymentMetaData(account.getDeviceInformation());
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
                if (payment.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL && !payment.getTransactionReceiptSent())
                    CompletableFuture.runAsync(() -> {
                        paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(payment);
                        payment.setTransactionReceiptSent(true);
                    });
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
        PaymentGateway payment = paymentGatewayRepo.findByRefNo(account.getRefNo()).orElse(null);
        if (payment == null) {
            return new ResponseEntity<>(new ErrorResponse("REFERENCE NUMBER DOESN'T EXIST"),
                    HttpStatus.BAD_REQUEST);
        }
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

            payment.setChannel(PaymentChannel.QR);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.PENDING);
            paymentGatewayRepo.save(payment);

            MerchantResponse merchant = merchantProxy.getMerchantInfo(token, payment.getMerchantId());
            if (!merchant.getCode().equals("00")) {
                return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();
            ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);
            payment.setChannel(PaymentChannel.QR);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.PENDING);

            WalletQRResponse tranRep = uniPaymentProxy.postQRTransaction(payment, token, account, profile);
            if (tranRep != null) {
                tranRep.setName(profile.getData().getOtherDetails().getOrganisationName());
                response = new ResponseEntity<>(new SuccessResponse("SUCCESS GENERATED", tranRep), HttpStatus.CREATED);
                payment.setTranDate(LocalDateTime.now());
                payment.setRcre_time(LocalDateTime.now());
                paymentGatewayRepo.save(payment);
            }
        } catch (Exception ex) {
            log.error("Error occurred - GET QR TRANSACTION :{}", ex.getMessage());
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED);
            paymentGatewayRepo.save(payment);
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
            //TODO: Update wayapay processing fee
            payment.setWayapayFee(account.getFee());
            payment.setCurrencyCode(account.getCurrency());
            payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
            payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
            payment.setCustomerName(account.getCustomer().getName());
            payment.setCustomerEmail(account.getCustomer().getEmail());
            payment.setCustomerPhone(account.getCustomer().getPhoneNumber());
            payment.setChannel(PaymentChannel.WALLET);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.PENDING);
            String vt = UnifiedPaymentProxy.getDataEncrypt(account.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
            payment.setSecretKey(vt);
            payment.setTranId(account.getReferenceNo());
            payment.setPreferenceNo(account.getReferenceNo());
            payment.setTranDate(LocalDateTime.now());
            payment.setRcre_time(LocalDateTime.now());
            paymentGatewayRepo.save(payment);
            return new ResponseEntity<>(new SuccessResponse("SUCCESS WALLET", strLong), HttpStatus.CREATED);

        } catch (Exception ex) {
            log.error("Error occurred - GET QR TRANSACTION :{}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> initiateUSSDTransaction(HttpServletRequest request, WayaUSSDRequest ussdRequest) {
        PaymentGateway payment = new PaymentGateway();
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
            MerchantResponse merchant = merchantProxy.getMerchantInfo(token, ussdRequest.getMerchantId());
            if (!merchant.getCode().equals("00")) {
                return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();
            if (sMerchant.getMerchantKeyMode().equals("TEST")) {
                if (!ussdRequest.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
                    return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
                }
            } else {
                if (!ussdRequest.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
                    return new ResponseEntity<>(new ErrorResponse("INVALID MERCHANT KEY"), HttpStatus.BAD_REQUEST);
                }
            }
            // Fetch Profile
            ProfileResponse profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);

            Date dte = new Date();
            long milliSeconds = dte.getTime();
            String strLong = Long.toString(milliSeconds);
            payment.setRefNo(strLong);
            payment.setMerchantId(ussdRequest.getMerchantId());
            payment.setDescription(ussdRequest.getPaymentDescription());
            payment.setAmount(ussdRequest.getAmount());
            //TODO: Update wayapay processing fee here
            payment.setProcessingFee(ussdRequest.getFee());
            payment.setCurrencyCode(ussdRequest.getCurrency());
            payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
            String vt = UnifiedPaymentProxy.getDataEncrypt(ussdRequest.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
            payment.setSecretKey(vt);
            payment.setTranId(ussdRequest.getReferenceNo());
            payment.setPreferenceNo(ussdRequest.getReferenceNo());
            payment.setTranDate(LocalDateTime.now());
            payment.setRcre_time(LocalDateTime.now());
            payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
            payment.setCustomerName(ussdRequest.getCustomer().getName());
            payment.setCustomerEmail(ussdRequest.getCustomer().getEmail());
            payment.setCustomerPhone(ussdRequest.getCustomer().getPhoneNumber());
            payment.setChannel(PaymentChannel.USSD);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.PENDING);
            payment.setVendorDate(LocalDateTime.now());

            BigDecimal wayapayFee = calculateWayapayFee(
                    ussdRequest.getMerchantId(), ussdRequest.getAmount(),
                    ProductName.USSD, "LOCAL", token);

            payment.setWayapayFee(wayapayFee);
            PaymentGateway pay = paymentGatewayRepo.save(payment);
            USSDResponse ussd = new USSDResponse();
            ussd.setRefNo(pay.getRefNo());
            ussd.setName(profile.getData().getOtherDetails().getOrganisationName());
            return new ResponseEntity<>(new SuccessResponse("SUCCESS USSD", ussd), HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Error occurred - GET USSD TRANSACTION :{0}", ex);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED);
            paymentGatewayRepo.save(payment);
            return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, WayaUSSDPayment account, String
            refNo) {
        //TODO: Query the transaction status again before updating the transaction
        PaymentGateway payment = paymentGatewayRepo.findByRefMerchant(refNo, account.getMerchantId()).orElse(null);
        if (payment == null)
            return new ResponseEntity<>(new ErrorResponse("NO PAYMENT REQUEST INITIATED"), HttpStatus.BAD_REQUEST);
        com.wayapaychat.paymentgateway.enumm.TransactionStatus status = com.wayapaychat.paymentgateway.enumm.TransactionStatus.valueOf(account.getStatus());
        payment.setStatus(status);
        payment.setTranId(account.getTranId());
        payment.setSuccessfailure(account.isSuccessfailure());
        payment.setChannel(PaymentChannel.USSD);
        LocalDateTime toDate = account.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        payment.setVendorDate(toDate);
        if (payment.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL && !payment.getTransactionReceiptSent())
            CompletableFuture.runAsync(() -> {
                paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(payment);
                payment.setTransactionReceiptSent(true);
            });
        ReportPayment reportPayment = modelMapper.map(paymentGatewayRepo.save(payment), ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("TRANSACTION UPDATE", reportPayment), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> queryTranStatus(HttpServletRequest req) {
        List<PaymentGateway> mPay = paymentGatewayRepo.findByPayment();
        if (mPay == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        List<ReportPayment> sPay = mapList(mPay, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> getMerchantTransactionReport(HttpServletRequest req, String merchantId) {
        @NotNull final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId, false);
        @NotNull List<PaymentGateway> paymentGatewayList;
        if (ObjectUtils.isEmpty(merchantIdToUse)) paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment();
        else paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment(merchantIdToUse);
        if (ObjectUtils.isEmpty(paymentGatewayList))
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> fetchAllMerchantTransactions(String merchantId) {
        @NotNull List<PaymentGateway> paymentGatewayList;
        paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment(merchantId);
        if (ObjectUtils.isEmpty(paymentGatewayList))
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getTransactionByRef(HttpServletRequest req, String refNo) {
        PaymentGateway mPay = null;
        try {
            mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
        } catch (Exception e) {
            log.info("---------||||ERROR||||---------", e);
        }
        if (mPay == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        Customer customer = new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId());
        TransactionStatusResponse response = new TransactionStatusResponse(mPay.getPreferenceNo(), mPay.getAmount(), mPay.getDescription(),
                mPay.getFee(), mPay.getCurrencyCode(), mPay.getStatus().name(), mPay.getChannel().name(),
                mPay.getMerchantName(), customer, mPay.getMerchantId(), mPay.getTranDate());
        return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
    }

    //TODO: Protect this method to check is user has access to operate Payment gateway
    @Override
    public ResponseEntity<?> abandonTransaction(HttpServletRequest request, String refNo, WayaPaymentStatus pay) {
        PaymentGateway mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
        if (mPay == null)
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        if (mPay.getStatus() != TransactionStatus.SUCCESSFUL) {
            mPay.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.ABANDONED);
            paymentGatewayRepo.save(mPay);
        }
        return new ResponseEntity<>(new SuccessResponse("Updated", "Success Updated"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest req, String merchantId) {
        @NotNull final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId, true);
        TransactionReportStats revenue = wayaPayment.getTransactionReportStats(merchantIdToUse);
        return new ResponseEntity<>(new SuccessResponse("GET REVENUE", revenue), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest req) {
        if (!PaymentGateWayCommonUtils.getAuthenticatedUser().getAdmin())
            throw new ApplicationException(403, "01", "Oops! Operation not allowed.");
        List<TransactionReportStats> revenue = wayaPayment.getTransactionReportStats();
        return new ResponseEntity<>(new SuccessResponse("LIST REVENUE", revenue), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest requests) {
        PaymentGateway payment = paymentGatewayRepo.findByTranId(requests.getTrxId()).orElse(null);
        if (payment == null)
            return ResponseEntity.badRequest().body("Ooops! TRANSACTION DOES NOT EXIST... FAILED TO COMPLETE TRANSACTION.");
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

    @Override
    public ResponseEntity<PaymentGatewayResponse> filterSearchCustomerTransactions(QueryCustomerTransactionPojo queryPojo, Pageable pageable) {
        AuthenticatedUser authenticatedUser = PaymentGateWayCommonUtils.getAuthenticatedUser();
        MerchantData merchantResponse = merchantProxy.getMerchantInfo(paymentGateWayCommonUtils.getDaemonAuthToken(), authenticatedUser.getMerchantId()).getData();
        queryPojo.setMerchantId(merchantResponse.getMerchantId());
        return new ResponseEntity<>(new SuccessResponse("Data fetched successfully",
                getCustomerTransaction(queryPojo, pageable)), HttpStatus.OK);
    }

    @Override
    public Page<PaymentGateway> getCustomerTransaction(QueryCustomerTransactionPojo queryPojo, Pageable pageable) {
        Page<PaymentGateway> result;
        String merchantId = queryPojo.getMerchantId();
        if (ObjectUtils.isNotEmpty(queryPojo.getStatus()) && ObjectUtils.isNotEmpty(queryPojo.getChannel()))
            result = paymentGatewayRepo.findByCustomerIdChannelStatus(
                    queryPojo.getCustomerId(), merchantId,
                    queryPojo.getStatus().name(), queryPojo.getChannel().name(), pageable);
        else if (ObjectUtils.isNotEmpty(queryPojo.getChannel()))
            result = paymentGatewayRepo.findByCustomerIdChannel(queryPojo.getCustomerId(), merchantId, queryPojo.getChannel().name(), pageable);
        else if (ObjectUtils.isNotEmpty(queryPojo.getStatus()))
            result = paymentGatewayRepo.findByStatus(queryPojo.getCustomerId(), merchantId, queryPojo.getStatus().name(), pageable);
        else result = paymentGatewayRepo.findByCustomerId(queryPojo.getCustomerId(), merchantId, pageable);
        return result;
    }

    private void preprocessTransactionStatus(PaymentGateway payment) {
        try {
            WayaTransactionQuery response = uniPaymentProxy.transactionQuery(payment.getTranId());
            log.info("-----UNIFIED PAYMENT RESPONSE {}----------", response);
            if (ObjectUtils.isNotEmpty(response)) {
                if (ObjectUtils.isNotEmpty(response.getStatus()) && response.getStatus().toUpperCase().equals(TStatus.APPROVED.name())) {
                    payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL);
                    payment.setSuccessfailure(true);
                    payment.setTranId(response.getOrderId());
                    payment.setProcessingFee(new BigDecimal(response.getConvenienceFee()));
                    if (payment.getIsFromRecurrentPayment()) {
                        updateRecurrentTransaction(payment);
                    }
                } else {
                    com.wayapaychat.paymentgateway.enumm.TransactionStatus transactionStatus = Arrays.stream(com.wayapaychat.paymentgateway.enumm.TransactionStatus.values()).map(Enum::name)
                            .collect(Collectors.toList())
                            .contains(response.getStatus().toUpperCase()) ? com.wayapaychat.paymentgateway.enumm.TransactionStatus.valueOf(response.getStatus().toUpperCase()) : com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED;
                    payment.setStatus(transactionStatus);
                    payment.setSuccessfailure(false);
                    payment.setTranId(response.getOrderId());
                }
                paymentGatewayRepo.save(payment);
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR||||-------", e);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED);
            paymentGatewayRepo.save(payment);
        }
    }

    @Override
    public void updateRecurrentTransaction(@NotNull final PaymentGateway paymentGateway) {
        if (paymentGateway.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL) {
            Optional<RecurrentTransaction> optionalRecurrentTransaction = recurrentTransactionRepository.getByTransactionRef(paymentGateway.getRefNo());
            if (optionalRecurrentTransaction.isPresent()) {
                LocalDateTime date = LocalDateTime.now();
                RecurrentTransaction foundRecurrentTransaction = optionalRecurrentTransaction.get();
                LocalDateTime chargeDateAfterFirstPayment = foundRecurrentTransaction.getNextChargeDateAfterFirstPayment();
                if (foundRecurrentTransaction.getTotalChargeCount() == 0)
                    foundRecurrentTransaction.setFirstPaymentDate(date);
                Integer totalChargeCount = foundRecurrentTransaction.getTotalChargeCount() + 1;
                foundRecurrentTransaction.setModifiedBy(0L);
                foundRecurrentTransaction.setDateModified(date);
                foundRecurrentTransaction.setActive(true);
                foundRecurrentTransaction.setStatus(RecurrentPaymentStatus.ACTIVE_RENEWING);
                foundRecurrentTransaction.setLastChargeDate(date);
                foundRecurrentTransaction.setUpSessionId(paymentGateway.getSessionId());
                foundRecurrentTransaction.setTotalChargeCount(totalChargeCount);
                foundRecurrentTransaction.setNextChargeDate(ObjectUtils.isEmpty(chargeDateAfterFirstPayment) ?
                        date.plusDays(foundRecurrentTransaction.getInterval()) : chargeDateAfterFirstPayment);
                recurrentTransactionRepository.save(foundRecurrentTransaction);

                SubscriptionEventPayload subscriptionEventPayload = SubscriptionEventPayload.builder()
                        .planId(foundRecurrentTransaction.getPlanId())
                        .merchantId(foundRecurrentTransaction.getMerchantId())
                        .customerSubscriptionId(foundRecurrentTransaction.getCustomerSubscriptionId())
                        .paymentLinkId(foundRecurrentTransaction.getPaymentLinkId())
                        .amountPaid(paymentGateway.getAmount())
                        .currencyCode(paymentGateway.getCurrencyCode())
                        .maxCount(foundRecurrentTransaction.getMaxChargeCount())
                        .currentCount(foundRecurrentTransaction.getTotalChargeCount())
                        .status(foundRecurrentTransaction.getStatus())
                        .nextChargeDate(foundRecurrentTransaction.getNextChargeDate())
                        .paymentDate(paymentGateway.getVendorDate())
                        .unsubscribed(false)
                        .paymentLinkType(foundRecurrentTransaction.getPaymentLinkType())
                        .build();

                ProducerMessageDto producerMessageDto = ProducerMessageDto.builder()
                        .data(subscriptionEventPayload)
                        .eventCategory(EventType.CUSTOMER_SUBSCRIPTION)
                        .build();

                messageQueueProducer.send("merchant", producerMessageDto);
            }
        }
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getMerchantYearMonthTransactionStats(String merchantId, Long year, Date startDate, Date endDate) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        List<TransactionYearMonthStats> transactionYearMonthStats = wayaPaymentDAO.getMerchantTransactionStatsByYearAndMonth(merchantIdToUse, year, startDate, endDate);
        BigDecimal totalRevenueForSelectedDateRange = transactionYearMonthStats.stream()
                .map(TransactionYearMonthStats::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = Map.of("dateRangeResult", transactionYearMonthStats,
                "totalRevenueForSelectedDateRange", totalRevenueForSelectedDateRange);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, result), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getMerchantTransactionOverviewStats(String merchantId) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        TransactionOverviewResponse transactionOverviewResponse = wayaPaymentDAO.getTransactionReport(merchantIdToUse);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, transactionOverviewResponse), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> getMerchantTransactionGrossAndNetRevenue(String merchantId) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        TransactionRevenueStats transactionRevenueStats = wayaPaymentDAO.getMerchantTransactionGrossAndNetRevenue(merchantIdToUse);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, transactionRevenueStats), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentGatewayResponse> fetchPaymentLinkTransactions(String merchantId, String paymentLinkId, Pageable pageable) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        Page<PaymentGateway> result;
        if (ObjectUtils.isEmpty(merchantIdToUse)) result = paymentGatewayRepo.getAllByPaymentLinkId(paymentLinkId, pageable);
        else result = paymentGatewayRepo.getAllByPaymentLinkId(merchantIdToUse, paymentLinkId, pageable);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, result), HttpStatus.OK);
    }

    private String replaceKeyPrefixWithEmptyString(String pub) {
        return pub.contains("WAYA") ?
                pub.replace("WAYAPUBK_TEST_0x", "") :
                pub.replace("WAYAPUBK_PROD_0x", "");
    }

    //REGION : LOCAL , INTERNATIONAL
    private BigDecimal calculateWayapayFee(
            String merchantId, BigDecimal amount, ProductName productName, String region, String token) {
        MerchantProductPricingQuery merchantProductPricingQuery = MerchantProductPricingQuery
                .builder()
                .merchantId(merchantId)
                .productName(productName)
                .build();
        MerchantProductPricingResponse merchantProductPricingResponse = iSettlementProductPricingProxy.getMerchantProductPricing(
                merchantProductPricingQuery.getMerchantId(), merchantProductPricingQuery.getProductName(), token
        );
        ProductPricingResponse productPricingResponse = merchantProductPricingResponse.getData();
        Double feePercentage = 0D;
        if (productPricingResponse.getLocalDiscountRate() > 0)
            feePercentage = productPricingResponse.getLocalDiscountRate();
        if (productPricingResponse.getLocalRate() > 0)
            feePercentage += productPricingResponse.getLocalRate();
        BigDecimal fee = amount.multiply(new BigDecimal(feePercentage)).divide(new BigDecimal(100), MathContext.DECIMAL64);
        if ((productName.equals(ProductName.WALLET) || productName.equals(ProductName.CARD) || productName.equals(ProductName.BANK)
                || productName.equals(ProductName.USSD)) || productName.equals(ProductName.PAYATTITUDE) && region.equals("LOCAL")) {
            BigDecimal cappedFee = productPricingResponse.getLocalProcessingFeeCappedAt();
            if (fee.compareTo(cappedFee) > 0)
                return cappedFee;
        }
        return fee;
    }
}

