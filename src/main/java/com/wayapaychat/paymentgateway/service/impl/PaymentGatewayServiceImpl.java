package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.common.enums.*;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAO;
import com.wayapaychat.paymentgateway.dao.WayaPaymentDAOImpl;
import com.wayapaychat.paymentgateway.entity.*;
import com.wayapaychat.paymentgateway.entity.listener.PaymemtGatewayEntityListener;
import com.wayapaychat.paymentgateway.enumm.*;
import com.wayapaychat.paymentgateway.exception.ApplicationException;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.kafkamessagebroker.producer.IkafkaMessageProducer;
import com.wayapaychat.paymentgateway.pojo.RolePermissionResponsePayload;
import com.wayapaychat.paymentgateway.pojo.RoleResponse;
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
import com.wayapaychat.paymentgateway.proxy.*;
import com.wayapaychat.paymentgateway.proxy.pojo.MerchantProductPricingQuery;
import com.wayapaychat.paymentgateway.repository.*;
import com.wayapaychat.paymentgateway.service.PaymentGatewayService;
import com.wayapaychat.paymentgateway.utility.Utility;
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
    private WalletProxy walletProxy;
    @Autowired
    private WithdrawalProxy withdrawalProxy;
    @Autowired
    private IdentityManagementServiceProxy identManager;
    @Autowired
    private ISettlementProductPricingProxy iSettlementProductPricingProxy;
    @Autowired
    private PaymentGatewayRepository paymentGatewayRepo;
    @Autowired
    private TokenizeRepository tokenizedRepo;
    @Autowired
    private SandboxPaymentGatewayRepository sandboxPaymentGatewayRepo;
    @Autowired
    private WalletProxy wallProxy;
    @Autowired
    private NIPTransferProxy nipTransferProxy;
    @Autowired
    private WayaPaymentDAO wayaPayment;
    @Autowired
    private PaymentWalletRepository paymentWalletRepo;
    
    @Autowired
    private WithdrawalRepository withdrawalRepository;
    
    @Autowired
    TransactionSettlementRepository transactionSettlementRepository;
    @Autowired
    private RecurrentTransactionRepository recurrentTransactionRepository;
    @Autowired
    private SandboxRecurrentTransactionRepository sandboxRecurrentTransactionRepository;
    @Value("${service.name}")
    private String username;
    @Value("${service.pass}")
    private String passSecret;
    @Value("${service.token}")
    private String DAEMON_TOKEN;
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
    
    @Autowired
    private RoleProxy roleProxy;
    
    @Autowired
    private ISWService iswService;
    
    @Autowired
    CardRepository cardRepository;

    // s-l done
    @Override
    public PaymentGatewayResponse initiateCardTransaction(HttpServletRequest request, WayaPaymentRequest transactionRequestPojo, Device device) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, "Unprocessed Transaction", null);
        try {
            MerchantResponse merchant = null;
            String token = paymentGateWayCommonUtils.getDaemonAuthToken();
            // get merchant data
            try {
                merchant = merchantProxy.getMerchantInfo(token, transactionRequestPojo.getMerchantId());
                if (merchant == null) {
                    return new PaymentGatewayResponse(false, "Profile doesn't exist", null);
                }
                
                if (!merchant.getCode().equals("00")) {
                    return new PaymentGatewayResponse(false, "Merchant id doesn't exist", null);
                }
                log.info("#_#_#_#_#_#_#_#_#_#_#_#_# MERCHANT FOUND #_#_#_#_#_#_#_#_#_#_#_#_#");
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }
            log.info("Merchant: " + merchant);
            MerchantData sMerchant = merchant.getData();

            // get merchant Profile
            ProfileResponse profile = null;
            try {
                profile = authProxy.getProfileDetail(sMerchant.getUserId(), token);
                if (profile == null) {
                    return new PaymentGatewayResponse(false, "Profile doesn't exist", null);
                }
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }

            // validate the provided merchant key
            if (sMerchant.getMerchantKeyMode().equals(MerchantTransactionMode.TEST.toString())) {
                if (!transactionRequestPojo.getWayaPublicKey().equals(sMerchant.getMerchantPublicTestKey())) {
                    log.info("TEST key found: " + sMerchant.getMerchantPublicTestKey());
                    log.info("TEST key received: " + transactionRequestPojo.getWayaPublicKey());
                    return new PaymentGatewayResponse(false, "Invalid merchant test key", null);
                }
            } else if (!transactionRequestPojo.getWayaPublicKey().equals(sMerchant.getMerchantProductionPublicKey())) {
                log.info("LIVE key found: " + sMerchant.getMerchantProductionPublicKey());
                log.info("LIVE key received: " + transactionRequestPojo.getWayaPublicKey());
                return new PaymentGatewayResponse(false, "Invalid merchant live key", null);
            }

            // Create customer record
            // IDM will determine the merchant key mode for creating the customer
            String[] customerName = transactionRequestPojo.getCustomer().getName().split("\\s+");
            CustomerRequest customer = new CustomerRequest();
            customer.setEmail(transactionRequestPojo.getCustomer().getEmail());
            customer.setMerchantPublicKey(transactionRequestPojo.getWayaPublicKey());
            customer.setPhoneNumber(transactionRequestPojo.getCustomer().getPhoneNumber());
            customer.setFirstName(ObjectUtils.isEmpty(customerName[0]) ? " " : customerName[0]);
            customer.setLastName(ObjectUtils.isEmpty(customerName[1]) ? " " : customerName[1]);
            MerchantCustomer merchantCustomer = identManager.postCustomerCreate(customer, token);
            
            Date dte = new Date();
            String strLong = Long.toString(dte.getTime()) + rnd.nextInt(999999);
            BigDecimal wayapayFee = calculateWayapayFee(
                    sMerchant.getMerchantId(), transactionRequestPojo.getAmount(),
                    ProductName.CARD, "LOCAL");
            String encryptedMerchantSecretKey = UnifiedPaymentProxy.getDataEncrypt(transactionRequestPojo.getWayaPublicKey(), encryptAllMerchantSecretKeyWith);
            CardResponse card = new CardResponse();
            String tranId = UUID.randomUUID() + "";
            if (!tranId.isBlank()) {
                card.setTranId(sMerchant.getMerchantKeyMode().equals(MerchantTransactionMode.PRODUCTION.toString()) ? strLong : "7263269" + strLong);
                card.setName(profile.getData().getOtherDetails().getOrganisationName());
                card.setCustomerId(merchantCustomer.getData().getCustomerId());
                card.setCustomerAvoid(merchantCustomer.getData().isCustomerAvoided());
                response = new PaymentGatewayResponse(true, "Success Transaction", card);
            }
            
            if (sMerchant.getMerchantKeyMode().equals(MerchantTransactionMode.PRODUCTION.toString())) {
                log.error("============================= PRODUCTION PAYMENT =================================");
                PaymentGateway payment = new PaymentGateway();
                payment.setRefNo(strLong);
                payment.setMerchantId(transactionRequestPojo.getMerchantId());
                payment.setMerchantEmail(merchant.getData().getMerchantEmailAddress());
                payment.setDescription(transactionRequestPojo.getDescription());
                payment.setAmount(transactionRequestPojo.getAmount());
                //TODO: update wayapay processing fee... update the region later
                // get the IP region of where the transaction was initiated from
                payment.setWayapayFee(wayapayFee);
                payment.setCustomerIpAddress(PaymentGateWayCommonUtils.getClientRequestIP(request));
                payment.setCurrencyCode(transactionRequestPojo.getCurrency());
                payment.setReturnUrl(sMerchant.getMerchantCallbackURL());
                payment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
                payment.setCustomerName(transactionRequestPojo.getCustomer().getName());
                payment.setCustomerEmail(transactionRequestPojo.getCustomer().getEmail());
                payment.setCustomerPhone(transactionRequestPojo.getCustomer().getPhoneNumber());
                payment.setStatus(TransactionStatus.PENDING);
                payment.setChannel(PaymentChannel.CARD);
                payment.setCustomerId(merchantCustomer.getData().getCustomerId());
                payment.setPreferenceNo(transactionRequestPojo.getPreferenceNo());
                payment.setSecretKey(encryptedMerchantSecretKey);
                payment.setPaymentLinkId(transactionRequestPojo.getPaymentLinkId());
                payment.setTranId(tranId);
                payment.setTranDate(LocalDateTime.now());
                payment.setRcre_time(LocalDateTime.now());
                payment.setVendorDate(LocalDateTime.now());
                paymentGatewayRepo.save(payment);
            } else {
                log.error("============================= SANDBOX PAYMENT =================================");
                SandboxPaymentGateway sandboxPayment = new SandboxPaymentGateway();
                sandboxPayment.setRefNo("7263269" + strLong);
                sandboxPayment.setMerchantId(transactionRequestPojo.getMerchantId());
                sandboxPayment.setMerchantEmail(merchant.getData().getMerchantEmailAddress());
                sandboxPayment.setDescription(transactionRequestPojo.getDescription());
                sandboxPayment.setAmount(transactionRequestPojo.getAmount());
                //TODO: update wayapay processing fee... update the region later
                // get the IP region of where the transaction was initiated from
                sandboxPayment.setWayapayFee(wayapayFee);
                sandboxPayment.setCustomerIpAddress(PaymentGateWayCommonUtils.getClientRequestIP(request));
                sandboxPayment.setCurrencyCode(transactionRequestPojo.getCurrency());
                sandboxPayment.setReturnUrl(sMerchant.getMerchantCallbackURL());
                sandboxPayment.setMerchantName(profile.getData().getOtherDetails().getOrganisationName());
                sandboxPayment.setCustomerName(transactionRequestPojo.getCustomer().getName());
                sandboxPayment.setCustomerEmail(transactionRequestPojo.getCustomer().getEmail());
                sandboxPayment.setCustomerPhone(transactionRequestPojo.getCustomer().getPhoneNumber());
                sandboxPayment.setStatus(TransactionStatus.PENDING);
                sandboxPayment.setChannel(PaymentChannel.CARD);
                sandboxPayment.setCustomerId(merchantCustomer.getData().getCustomerId());
                sandboxPayment.setPreferenceNo(transactionRequestPojo.getPreferenceNo());
                sandboxPayment.setSecretKey(encryptedMerchantSecretKey);
                sandboxPayment.setPaymentLinkId(transactionRequestPojo.getPaymentLinkId());
                sandboxPayment.setTranId(tranId);
                sandboxPayment.setTranDate(LocalDateTime.now());
                sandboxPayment.setRcre_time(LocalDateTime.now());
                sandboxPayment.setVendorDate(LocalDateTime.now());
                sandboxPaymentGatewayRepo.save(sandboxPayment);
            }
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Wahala wey no gree finish ", ex);
        }
        return response;
    }

    // s-l done
    @Override
    public void preprocessRecurrentPayment(UnifiedCardRequest cardRequest, WayaCardPayment card, Object paymentGatewayData, String mode) {
        //TODO: UP For pay attitude, These fields are not present to tell when the
        // The recurring payment should happen
        // frequency , OrderExpirationPeriod
        @NotNull
        final String ORDER_TYPE = "Purchase";
        @NotNull
        final String DATE_SEPARATOR = "/";
        @NotNull
        final String PAY_ATTITUDE = "PayAttitude";
        String customerId;
        PaymentGateway paymentGateway = new PaymentGateway();
        SandboxPaymentGateway sandboxPaymentGateway = new SandboxPaymentGateway();
        if (mode == MerchantTransactionMode.PRODUCTION.toString()) {
            modelMapper.map(paymentGatewayData, paymentGateway);
            customerId = paymentGateway.getCustomerId();
        } else {
            modelMapper.map(paymentGatewayData, sandboxPaymentGateway);
            customerId = sandboxPaymentGateway.getCustomerId();
        }
        PaymentLinkResponse paymentLinkResponse = identManager.getPaymentLinkDetailsById(paymentGateWayCommonUtils.getDaemonAuthToken(), card.getPaymentLinkId()).getData();
        cardRequest.setRecurring(true);
        
        if (paymentLinkResponse.getPaymentLinkType() == PaymentLinkType.ONE_TIME_PAYMENT_LINK) {
            throw new ApplicationException(403, "01", "One time payment link can't be used for recurrent payment");
        } else if (paymentLinkResponse.getIntervalType() == null) {
            throw new ApplicationException(403, "01", "Payment link does not have interval type. "
                    + "Kindly provide one with recurrent interval to charge customer");
        } else if (paymentLinkResponse.getPaymentLinkType() == PaymentLinkType.CUSTOMER_SUBSCRIPTION_PAYMENT_LINK
                && !Objects.equals(customerId, paymentLinkResponse.getBelongsToCustomerId())) {
            throw new ApplicationException(403, "01", String.format("Payment link does not belong to this customer %s", customerId)
                    + "Kindly provide one with recurrent interval to charge customer");
        } else if (paymentLinkResponse.getLinkCanExpire() && ObjectUtils.isNotEmpty(paymentLinkResponse.getExpiryDate())) {
            if (paymentLinkResponse.getExpiryDate().isAfter(LocalDateTime.now())) {
                throw new ApplicationException(403, "01", "Payment link has expired and can't not be used to process payment");
            }
        }
        
        if (mode == MerchantTransactionMode.PRODUCTION.toString()) {
            Optional<RecurrentTransaction> optionalRecurrentPayment = recurrentTransactionRepository.getByTransactionRef(paymentGateway.getRefNo());
            RecurrentTransaction recurrentTransaction = null;
            if (optionalRecurrentPayment.isPresent()) {
                recurrentTransaction = optionalRecurrentPayment.get();
                if (recurrentTransaction.getActive()) {
                    throw new ApplicationException(403, "01", "Recurrent payment still active. Payment can't be processed");
                }
                if (ObjectUtils.isNotEmpty(recurrentTransaction.getNextChargeDate()) && recurrentTransaction.getNextChargeDate().isBefore(LocalDateTime.now())) {
                    throw new ApplicationException(403, "01", "Recurrent payment has not yet expired.");
                } else {
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
        } else {
            Optional<SandboxRecurrentTransaction> optionalSandboxRecurrentPayment = sandboxRecurrentTransactionRepository.getByTransactionRef(sandboxPaymentGateway.getRefNo());
            SandboxRecurrentTransaction sandboxRecurrentTransaction = null;
            if (optionalSandboxRecurrentPayment.isPresent()) {
                sandboxRecurrentTransaction = optionalSandboxRecurrentPayment.get();
                if (sandboxRecurrentTransaction.getActive()) {
                    throw new ApplicationException(403, "01", "Recurrent payment still active. Payment can't be processed");
                }
                if (ObjectUtils.isNotEmpty(sandboxRecurrentTransaction.getNextChargeDate()) && sandboxRecurrentTransaction.getNextChargeDate().isBefore(LocalDateTime.now())) {
                    throw new ApplicationException(403, "01", "Recurrent payment has not yet expired.");
                } else {
                    sandboxRecurrentTransaction.setCurrentTransactionRefNo(sandboxPaymentGateway.getRefNo());
                    sandboxRecurrentTransaction.setDateModified(LocalDateTime.now());
                    sandboxRecurrentTransaction.setModifiedBy(0L);
                    preprocessCardRequest(paymentLinkResponse, cardRequest);
                    return;
                }
            }
            
            sandboxRecurrentTransaction = SandboxRecurrentTransaction.
                    builder()
                    .active(false)
                    .paymentLinkId(paymentLinkResponse.getPaymentLinkId())
                    .paymentLinkType(paymentLinkResponse.getPaymentLinkType())
                    .intervalType(paymentLinkResponse.getIntervalType())
                    .interval(paymentLinkResponse.getInterval())
                    .recurrentAmount(paymentLinkResponse.getPayableAmount())
                    .nextChargeDate(LocalDateTime.now().plusDays(paymentLinkResponse.getInterval()))
                    .customerId(sandboxPaymentGateway.getCustomerId())
                    .customerSubscriptionId(paymentLinkResponse.getCustomerSubscriptionId())
                    .maxChargeCount(paymentLinkResponse.getTotalCount())
                    .merchantId(sandboxPaymentGateway.getMerchantId())
                    .currentTransactionRefNo(sandboxPaymentGateway.getRefNo())
                    .planId(paymentLinkResponse.getPlanId())
                    .nextChargeDateAfterFirstPayment(paymentLinkResponse.getStartDateAfterFirstPayment())
                    .build();
            
            if (card.getScheme().equals(PAY_ATTITUDE)) {
//            cardRequest.setCount(0);
//            cardRequest.setOrderType(ORDER_TYPE);
            }
            preprocessCardRequest(paymentLinkResponse, cardRequest);
            sandboxRecurrentTransaction = sandboxRecurrentTransactionRepository.save(sandboxRecurrentTransaction);
            sandboxPaymentGateway.setRecurrentPaymentId(sandboxRecurrentTransaction.getId());
            sandboxPaymentGateway.setPaymentLinkId(sandboxRecurrentTransaction.getPaymentLinkId());
            sandboxPaymentGateway.setIsFromRecurrentPayment(true);
        }
    }

    // s-l done
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

    // s-l done
    @Override
    public ResponseEntity<?> processPaymentWithCard(HttpServletRequest request, WayaCardPayment card) throws JsonProcessingException {
        UnifiedCardRequest upCardPaymentRequest = new UnifiedCardRequest();
        RecurrentTransaction recurrentTransaction;
        SandboxPaymentGateway sandboxPaymentGateway = new SandboxPaymentGateway();
        PaymentGateway paymentGateway = new PaymentGateway();
        String mode = "";
        
        if (card.getTranId().startsWith("7263269")) {
            mode = MerchantTransactionMode.TEST.name();
            // process as test payment
            Optional<SandboxPaymentGateway> optionalSandboxPaymentGateway = sandboxPaymentGatewayRepo.findByRefNo(card.getTranId());
            if (optionalSandboxPaymentGateway.isEmpty()) {
                return new ResponseEntity<>(new ErrorResponse("Transaction does not exists in sandbox"), HttpStatus.BAD_REQUEST);
            }
            
            sandboxPaymentGateway = optionalSandboxPaymentGateway.get();
            if (sandboxPaymentGateway.getTransactionExpired()) {
                if (!(sandboxPaymentGateway.getStatus() == TransactionStatus.ABANDONED)) {
                    sandboxPaymentGateway.setStatus(TransactionStatus.ABANDONED);
                    SandboxPaymentGateway finalSandboxPaymentGateway = sandboxPaymentGateway;
                    CompletableFuture.runAsync(() -> sandboxPaymentGatewayRepo.save(finalSandboxPaymentGateway));
                }
                throw new ApplicationException(400, "01", String.format("Oops! Sandbox Transaction with transaction reference %s has expired!", sandboxPaymentGateway.getRefNo()));
            } else if (card.isRecurrentPayment()) {
                if (ObjectUtils.isEmpty(card.getPaymentLinkId())) {
                    throw new ApplicationException(400, "01", "Sandbox Recurrent payment link Id is required");
                }
                preprocessRecurrentPayment(upCardPaymentRequest, card, sandboxPaymentGateway, MerchantTransactionMode.TEST.toString());
            } else if (sandboxPaymentGateway.getStatus() == TransactionStatus.SUCCESSFUL) {
                return new ResponseEntity<>(new ErrorResponse("Sandbox Transaction already successful"), HttpStatus.FORBIDDEN);
            }
        } else {
            mode = MerchantTransactionMode.PRODUCTION.name();
            // process as live payment
            Optional<PaymentGateway> optionalPaymentGateway = paymentGatewayRepo.findByRefNo(card.getTranId());
            if (optionalPaymentGateway.isEmpty()) {
                return new ResponseEntity<>(new ErrorResponse("Transaction does not exists"), HttpStatus.BAD_REQUEST);
            }
            
            paymentGateway = optionalPaymentGateway.get();
            if (paymentGateway.getTransactionExpired()) {
                if (!(paymentGateway.getStatus() == TransactionStatus.ABANDONED)) {
                    paymentGateway.setStatus(TransactionStatus.ABANDONED);
                    PaymentGateway finalPaymentGateway = paymentGateway;
                    CompletableFuture.runAsync(() -> paymentGatewayRepo.save(finalPaymentGateway));
                }
                throw new ApplicationException(400, "01", String.format("Oops! Transaction with transaction reference %s has expired!", paymentGateway.getRefNo()));
            } else if (card.isRecurrentPayment()) {
                if (ObjectUtils.isEmpty(card.getPaymentLinkId())) {
                    throw new ApplicationException(400, "01", "Recurrent payment link Id is required");
                }
                preprocessRecurrentPayment(upCardPaymentRequest, card, paymentGateway, MerchantTransactionMode.PRODUCTION.toString());
            } else if (paymentGateway.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL) {
                return new ResponseEntity<>(new ErrorResponse("Transaction already successful"), HttpStatus.FORBIDDEN);
            }
        }
        
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
            if (mt.length < 2) {
                throw new ApplicationException(400, "01", "Card missing all correct fields. Ensure card is encrypted properly.");
            }
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
            if (mt.length < 2) {
                throw new ApplicationException(400, "01", "Card missing all correct fields. Ensure card is encrypted properly.");
            }
            pan = mt[0];
            String cvv = mt[1];
            upCardPaymentRequest.setCardNumber(pan);
            upCardPaymentRequest.setCvv(cvv);
            log.info("Card Info: " + upCardPaymentRequest);
        } else if (card.getScheme().equalsIgnoreCase("PayAttitude")) {
            //TODO: Get the country IP Address to be able to charge the customer with fee
            upCardPaymentRequest.setCvv(card.getEncryptCardNo());
            if (card.getTranId().startsWith("7263269")) { // merchant transacts in test mode
                sandboxPaymentGateway.setChannel(PaymentChannel.PAYATTITUDE);
                BigDecimal wayapayFee = calculateWayapayFee(
                        sandboxPaymentGateway.getMerchantId(), sandboxPaymentGateway.getAmount(),
                        ProductName.PAYATTITUDE, "LOCAL");
                sandboxPaymentGateway.setWayapayFee(wayapayFee);
            } else { // merchant transacts in live mode
                paymentGateway.setChannel(PaymentChannel.PAYATTITUDE);
                BigDecimal wayapayFee = calculateWayapayFee(
                        paymentGateway.getMerchantId(), paymentGateway.getAmount(),
                        ProductName.PAYATTITUDE, "LOCAL");
                paymentGateway.setWayapayFee(wayapayFee);
            }
            log.info("Card Info: " + upCardPaymentRequest);
        }
        response = new PaymentGatewayResponse(false, "Encrypt Card fail", null);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String encryptData = uniPaymentProxy.encryptPaymentDataAccess(upCardPaymentRequest, mode);
        if (!encryptData.isBlank()) {
            response = new PaymentGatewayResponse(true, "Success Encrypt", encryptData);
            httpStatus = HttpStatus.OK;
        }
        if (card.getTranId().startsWith("7263269")) { // merchant transacts in test mode
            sandboxPaymentGateway.setPaymentMetaData(card.getDeviceInformation());
            sandboxPaymentGateway.setScheme(card.getScheme());
            sandboxPaymentGateway.setMaskedPan(PaymentGateWayCommonUtils.maskedPan(pan));
            sandboxPaymentGatewayRepo.save(sandboxPaymentGateway);
        } else { // merchant transacts in live mode
            paymentGateway.setPaymentMetaData(card.getDeviceInformation());
            paymentGateway.setScheme(card.getScheme());
            paymentGateway.setMaskedPan(PaymentGateWayCommonUtils.maskedPan(pan));
            paymentGatewayRepo.save(paymentGateway);
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    // s-l done
    @Override
    public PaymentGatewayResponse processCardTransaction(HttpServletRequest request, HttpServletResponse response, WayaPaymentCallback pay) {
        PaymentGatewayResponse mResponse = new PaymentGatewayResponse(false, "Callback fail", null);
        String tranId = null;
        String mode = "";
        try {
            if (pay.getTranId().startsWith("7263269")) { // merchant transacts in test mode
                mode = MerchantTransactionMode.TEST.name();
                SandboxPaymentGateway msPay = sandboxPaymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
                if (msPay != null) {
                    msPay.setEncyptCard(pay.getCardEncrypt());
                    msPay.setChannel(PaymentChannel.CARD);
                    WayaPaymentRequest mAccount = new WayaPaymentRequest(msPay.getMerchantId(), msPay.getDescription(),
                            msPay.getAmount(), msPay.getFee(), msPay.getCurrencyCode(), msPay.getSecretKey(),
                            new Customer(msPay.getCustomerName(), msPay.getCustomerEmail(), msPay.getCustomerPhone(), msPay.getCustomerId()),
                            msPay.getPreferenceNo(), MerchantTransactionMode.TEST.name());
                    // step 1 - send to unified payment for initialization. this will return a string id
                    tranId = uniPaymentProxy.postUnified(mAccount);
                    if (ObjectUtils.isEmpty(tranId)) {
//                        msPay.setStatus(TransactionStatus.FAILED);
//                        sandboxPaymentGatewayRepo.save(msPay);
                        return new PaymentGatewayResponse(false, "Failed to initiate post tranId for 3D Authentication.", null);
                    }
                    msPay.setTranId(tranId);
                    sandboxPaymentGatewayRepo.save(msPay);
                }
            } else { // merchant transacts in live mode
                mode = MerchantTransactionMode.PRODUCTION.name();
                PaymentGateway mPay = paymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
                if (mPay != null) {
                    mPay.setEncyptCard(pay.getCardEncrypt());
                    mPay.setChannel(PaymentChannel.CARD);
                    WayaPaymentRequest mAccount = new WayaPaymentRequest(mPay.getMerchantId(), mPay.getDescription(),
                            mPay.getAmount(), mPay.getFee(), mPay.getCurrencyCode(), mPay.getSecretKey(),
                            new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId()),
                            mPay.getPreferenceNo(), MerchantTransactionMode.PRODUCTION.name());
                    // step 1 - send to unified payment for initialization. this will return a string id
                    tranId = uniPaymentProxy.postUnified(mAccount);
                    if (ObjectUtils.isEmpty(tranId)) {
//                        mPay.setStatus(TransactionStatus.FAILED);
//                        paymentGatewayRepo.save(mPay);
                        return new PaymentGatewayResponse(false, "Failed to initiate post tranId for 3D Authentication.", null);
                    }
                    mPay.setTranId(tranId);
                    paymentGatewayRepo.save(mPay);
                }
            }
            String callReq = uniPaymentProxy.buildUnifiedPaymentURLWithPayload(tranId, pay.getCardEncrypt(), false, mode);
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
        } catch (Exception ex) {
            log.error("----|||| ERROR OCCURRED {0} ||||----", ex);
            return new PaymentGatewayResponse(false, "Transaction Not Completed", null);
        }
        return mResponse;
    }

    // s-l done
    @Override
    public PaymentGatewayResponse payAttitudeCallback(HttpServletRequest request, WayaPaymentCallback pay) {
        PaymentGatewayResponse response = new PaymentGatewayResponse(false, "PayAttitude Callback fail", null);
        String tranId = null;
        WayaPayattitude attitude = null;
        String mode = "";
        
        if (pay.getTranId().startsWith("7263269")) { // merchant transacts in test mode
            mode = MerchantTransactionMode.TEST.name();
            SandboxPaymentGateway msPay = sandboxPaymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
            if (msPay != null) {
                msPay.setEncyptCard(pay.getCardEncrypt());
                msPay.setChannel(PaymentChannel.PAYATTITUDE);
                
                WayaPaymentRequest mAccount = new WayaPaymentRequest(msPay.getMerchantId(), msPay.getDescription(),
                        msPay.getAmount(), msPay.getFee(), msPay.getCurrencyCode(), msPay.getSecretKey(),
                        new Customer(msPay.getCustomerName(), msPay.getCustomerEmail(), msPay.getCustomerPhone(), msPay.getCustomerId()),
                        msPay.getPreferenceNo(), MerchantTransactionMode.TEST.name());
                tranId = uniPaymentProxy.postUnified(mAccount);
                if (ObjectUtils.isEmpty(tranId)) {
                    return new PaymentGatewayResponse(false, "Failed to process transaction authentication. Please try again later!", null);
                }
                msPay.setTranId(tranId);
                sandboxPaymentGatewayRepo.save(msPay);
                
                attitude = new WayaPayattitude(tranId, pay.getCardEncrypt());
            }
        } else { // merchant transacts in live mode
            mode = MerchantTransactionMode.PRODUCTION.name();
            PaymentGateway mPay = paymentGatewayRepo.findByRefNo(pay.getTranId()).orElse(null);
            if (mPay != null) {
                mPay.setEncyptCard(pay.getCardEncrypt());
                mPay.setChannel(PaymentChannel.PAYATTITUDE);
                
                WayaPaymentRequest mAccount = new WayaPaymentRequest(mPay.getMerchantId(), mPay.getDescription(),
                        mPay.getAmount(), mPay.getFee(), mPay.getCurrencyCode(), mPay.getSecretKey(),
                        new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId()),
                        mPay.getPreferenceNo(), MerchantTransactionMode.PRODUCTION.name());
                tranId = uniPaymentProxy.postUnified(mAccount);
                if (ObjectUtils.isEmpty(tranId)) {
                    return new PaymentGatewayResponse(false, "Failed to process transaction authentication. Please try again later!", null);
                }
                mPay.setTranId(tranId);
                paymentGatewayRepo.save(mPay);
                
                attitude = new WayaPayattitude(tranId, pay.getCardEncrypt());
            }
        }
        
        if (attitude != null) {
            WayaTransactionQuery callReq = uniPaymentProxy.postPayAttitude(attitude, mode);
            if (callReq != null) {
                response = new PaymentGatewayResponse(true, "Success Encrypt", callReq);
            }
        }
        
        return response;
    }
    
    @Override
    public ResponseEntity<?> getTransactionStatus(HttpServletRequest req, String tranId) {
        WayaTransactionQuery response = null;
        String mode = "";
        try {
            if (tranId.startsWith("7263269")) {
                mode = MerchantTransactionMode.TEST.name();
            } else {
                mode = MerchantTransactionMode.PRODUCTION.name();
            }
            response = uniPaymentProxy.transactionQuery(tranId, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response == null) {
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
    }
    
    @Override
    public WayaTransactionQuery getTransactionStatus(String tranId) {
        WayaTransactionQuery response = null;
        String mode = "";
        try {
            if (tranId.startsWith("7263269")) {
                mode = MerchantTransactionMode.TEST.name();
            } else {
                mode = MerchantTransactionMode.PRODUCTION.name();
            }
            response = uniPaymentProxy.transactionQuery(tranId, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    // s-l done
    @Override
    public PaymentGatewayResponse encryptCard(HttpServletRequest request, WayaEncypt pay) {
        String keygen = replaceKeyPrefixWithEmptyString(pay.getMerchantPublicKey());
        String vt = UnifiedPaymentProxy.getDataEncrypt(pay.getEncryptString(), keygen);
        if (ObjectUtils.isEmpty(vt)) {
            return (new PaymentGatewayResponse(false, "Encryption fail", null));
        }
        return (new PaymentGatewayResponse(true, "Encrypted", vt));
    }

    // s-l done
    @Override
    public PaymentGatewayResponse decryptCard(HttpServletRequest request, WayaDecypt pay) {
        String keygen = replaceKeyPrefixWithEmptyString(pay.getMerchantPublicKey());
        String vt = UnifiedPaymentProxy.getDataDecrypt(pay.getDecryptString(), keygen);
        if (ObjectUtils.isEmpty(vt)) {
            return (new PaymentGatewayResponse(false, "Decryption fail", null));
        }
        return (new PaymentGatewayResponse(true, "Decrypted", vt));
    }

    // s-l done
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
        if (account.getRefNo().startsWith("7263269")) {
            SandboxPaymentGateway sandboxPayment;
            try {
                sandboxPayment = sandboxPaymentGatewayRepo.findByRefNo(account.getRefNo()).orElse(null);
                if (sandboxPayment == null) {
                    return new ResponseEntity<>(new ErrorResponse("REFERENCE NUMBER DOESN'T EXIST"),
                            HttpStatus.BAD_REQUEST);
                }
                sandboxPayment.setChannel(PaymentChannel.WALLET);
                sandboxPaymentGatewayRepo.save(sandboxPayment);
                
                if (sandboxPayment.isSuccessfailure() && sandboxPayment.getStatus().name().equals("SUCCESSFUL")) {
                    return new ResponseEntity<>(
                            new ErrorResponse("TRANSACTION ALREADY COMPLETED FOR REFERENCE NUMBER :" + sandboxPayment.getRefNo()),
                            HttpStatus.BAD_REQUEST);
                }

                // validate that the merchant exists -- uncomment this code when role and access is ready
                // currently it have been commented because the endpoints does not permit merchant to view other merchants
                // info
//                MerchantResponse merchant = merchantProxy.getMerchantInfo(token, sandboxPayment.getMerchantId());
//                if (!merchant.getCode().equals("00")) {
//                    return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
//                }
//                log.info("Merchant: " + merchant);
//                MerchantData sMerchant = merchant.getData();
//                log.info("Merchant ID: " + sMerchant.getMerchantId());
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

//                PaymentWallet wallet = new PaymentWallet();
//                FundEventResponse tran = uniPaymentProxy.postWalletTransaction(account, token, sandboxPayment);
//                if (tran != null) {
                Date dte = new Date();
                String strLong = "7263269" + Long.toString(dte.getTime()) + rnd.nextInt(999999);
                response = new ResponseEntity<>(new SuccessResponse("SUCCESS TRANSACTION", strLong),
                        HttpStatus.CREATED);
                sandboxPayment.setTranId(strLong);
//                    sandboxPayment.setTranId(tran.getTranId());
                sandboxPayment.setTranDate(LocalDateTime.now());
                sandboxPayment.setRcre_time(LocalDateTime.now());
                sandboxPayment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL);
                sandboxPayment.setChannel(PaymentChannel.WALLET);
                //TODO: update wayapay processing fee... update the region later
                // get the IP region of where the transaction was initiated from
                // Change sandboxPayment.getMerchantId() to sMerchant.getMerchantId()
                BigDecimal wayapayFee = calculateWayapayFee(sandboxPayment.getMerchantId(), sandboxPayment.getAmount(),
                        ProductName.WALLET, "LOCAL");
                sandboxPayment.setWayapayFee(wayapayFee);
                
                sandboxPayment.setPaymentMetaData(account.getDeviceInformation());
                sandboxPayment.setSuccessfailure(true);
                sandboxPaymentGatewayRepo.save(sandboxPayment);

//                    wallet.setPaymentDescription(tran.getTranNarrate());
//                    wallet.setPaymentReference(tran.getPaymentReference());
//                    wallet.setTranAmount(tran.getTranAmount());
//                    wallet.setTranDate(tran.getTranDate());
//                    wallet.setTranId(tran.getTranId());
//                    wallet.setRefNo(payment.getRefNo());
//                    wallet.setSettled(TransactionSettled.NOT_SETTLED);
//                    wallet.setStatus(TStatus.APPROVED);
//                    if (sandboxPayment.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL && !sandboxPayment.getTransactionReceiptSent())
//                        CompletableFuture.runAsync(() -> {
//                            paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(sandboxPayment);
//                            sandboxPayment.setTransactionReceiptSent(true);
//                        });
//                    paymentWalletRepo.save(wallet);
//                } else {
//                    wallet.setPaymentDescription(payment.getDescription());
//                    wallet.setPaymentReference(payment.getPreferenceNo());
//                    wallet.setTranAmount(payment.getAmount());
//                    wallet.setStatus(TStatus.REJECTED);
//                    paymentWalletRepo.save(wallet);
//                }
            } catch (Exception ex) {
                log.error("Error occurred - GET SANDBOX WALLET TRANSACTION :{}", ex.getMessage());
                return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            }
        } else {
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

                // validate that the merchant exists -- uncomment this code when role and access is ready
                // currently it have been commented because the endpoints does not permit merchant to view other merchants
                // info
//                MerchantResponse merchant = merchantProxy.getMerchantInfo(token, payment.getMerchantId());
//                if (!merchant.getCode().equals("00")) {
//                    return new ResponseEntity<>(new ErrorResponse("MERCHANT ID DOESN'T EXIST"), HttpStatus.BAD_REQUEST);
//                }
//                log.info("Merchant: " + merchant);
//                MerchantData sMerchant = merchant.getData();
//                log.info("Merchant ID: " + sMerchant.getMerchantId());
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
                FundEventResponse tran = uniPaymentProxy.postWalletTransaction(account, token, payment, mAuth.getId());
                if (tran != null) {
                    response = new ResponseEntity<>(new SuccessResponse("SUCCESS TRANSACTION", tran.getTranId()),
                            HttpStatus.CREATED);
                    payment.setTranId(tran.getTranId());
                    payment.setTranDate(LocalDateTime.now());
                    payment.setRcre_time(LocalDateTime.now());
                    payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL);
                    payment.setChannel(PaymentChannel.WALLET);
                    //TODO: update wayapay processing fee... update the region later
                    // get the IP region of where the transaction was initiated from.
                    // Change payment.getMerchantId() to sMerchant.getMerchantId()
                    BigDecimal wayapayFee = calculateWayapayFee(payment.getMerchantId(), payment.getAmount(),
                            ProductName.WALLET, "LOCAL");
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
                    if (payment.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL && !payment.getTransactionReceiptSent()) {
                        CompletableFuture.runAsync(() -> {
                            paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(payment);
                            payment.setTransactionReceiptSent(true);
                        });
                    }
                    paymentWalletRepo.save(wallet);

                    //save settlement
                    DefaultWalletResponse merchantDefaultWallet = walletProxy.getUserDefaultWalletAccount(token, mAuth.getId());
                    TransactionSettlement transactionSettlement = new TransactionSettlement();
                    transactionSettlement.setSettlementReferenceId(tran.getTranId());
                    transactionSettlement.setMerchantId(payment.getMerchantId());
                    transactionSettlement.setSettlementNetAmount(payment.getAmount());
                    transactionSettlement.setMerchantUserId(mAuth.getId());
                    transactionSettlement.setSettlementAccount(merchantDefaultWallet.getData().getAccountNo());
                    transactionSettlement.setSettlementGrossAmount(payment.getAmount());
                    transactionSettlement.setSettlementStatus(SettlementStatus.PENDING);
                    transactionSettlement.setCreatedBy(mAuth.getId());
                    transactionSettlement.setDateCreated(LocalDateTime.now());
                    transactionSettlementRepository.save(transactionSettlement);
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
                    ProductName.USSD, "LOCAL");
            
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
    public ResponseEntity<?> updateUSSDTransaction(HttpServletRequest request, WayaUSSDPayment account, String refNo) {
        //TODO: Query the transaction status again before updating the transaction
        PaymentGateway payment = paymentGatewayRepo.findByRefMerchant(refNo, account.getMerchantId()).orElse(null);
        if (payment == null) {
            return new ResponseEntity<>(new ErrorResponse("NO PAYMENT REQUEST INITIATED"), HttpStatus.BAD_REQUEST);
        }
        com.wayapaychat.paymentgateway.enumm.TransactionStatus status = com.wayapaychat.paymentgateway.enumm.TransactionStatus.valueOf(account.getStatus());
        payment.setStatus(status);
        payment.setTranId(account.getTranId());
        payment.setSuccessfailure(account.isSuccessfailure());
        payment.setChannel(PaymentChannel.USSD);
        LocalDateTime toDate = account.getTranDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        payment.setVendorDate(toDate);
        if (payment.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL && !payment.getTransactionReceiptSent()) {
            CompletableFuture.runAsync(() -> {
                paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(payment);
                payment.setTransactionReceiptSent(true);
            });
        }
        ReportPayment reportPayment = modelMapper.map(paymentGatewayRepo.save(payment), ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("TRANSACTION UPDATE", reportPayment), HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<?> queryTranStatus(HttpServletRequest req) {
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
        @NotNull
        final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId, false);
        @NotNull
        List<PaymentGateway> paymentGatewayList;
        if (ObjectUtils.isEmpty(merchantIdToUse)) {
            paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment();
        } else {
            paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment(merchantIdToUse);
        }
        if (ObjectUtils.isEmpty(paymentGatewayList)) {
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
        }
        final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
        return new ResponseEntity<>(new SuccessResponse("List Payment", sPay), HttpStatus.OK);
    }

    // s-l done
    @Override
    public ResponseEntity<?> fetchAllMerchantTransactions(String merchantId, String token) {
        MerchantResponse merchant = null;
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, merchantId);
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }

//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchant.getData().getUserId(), token);
//        if(response.getPermissions().contains(MerchantPermissions.CAN_VIEW_TRANSACTIONS)) {
        if (merchant.getData().getMerchantKeyMode().equals(MerchantTransactionMode.PRODUCTION.toString())) {
            @NotNull
            List<PaymentGateway> paymentGatewayList;
            paymentGatewayList = this.paymentGatewayRepo.findByMerchantPayment(merchantId);
            if (ObjectUtils.isEmpty(paymentGatewayList)) {
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH LIVE PAYMENTS"), HttpStatus.BAD_REQUEST);
            }
            final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
            return new ResponseEntity<>(new SuccessResponse("Payment List", sPay), HttpStatus.OK);
        } else {
            @NotNull
            List<SandboxPaymentGateway> paymentGatewayList;
            paymentGatewayList = this.sandboxPaymentGatewayRepo.findByMerchantPayment(merchantId);
            if (ObjectUtils.isEmpty(paymentGatewayList)) {
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH SANDBOX PAYMENTS"), HttpStatus.BAD_REQUEST);
            }
            final List<ReportPayment> sPay = mapList(paymentGatewayList, ReportPayment.class);
            return new ResponseEntity<>(new SuccessResponse("Sandbox Payment List", sPay), HttpStatus.OK);
        }
//        }
//        else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR, null), HttpStatus.NOT_FOUND);
//        }
    }
    
    @Override
    public PaymentGatewayResponse getWalletBalance(HttpServletRequest request, String merchantId, String token) {
        @NotNull
        final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId, true);
        
        MerchantResponse merchant = null;
        String mode = null;
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, merchantId);
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new PaymentGatewayResponse("Profile doesn't exist", HttpStatus.NOT_FOUND);
            }
            mode = merchant.getData().getMerchantKeyMode();
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
        
        List<PaymentGateway> totalSuccessfulTransactions = paymentGatewayRepo.findPaymentBySuccessfulStatus(merchantId);
        List<PaymentGateway> totalTransactionsSettled = paymentGatewayRepo.findPaymentBySettledStatus(merchantId);
        List<Withdrawals> totalWithdrawals = withdrawalRepository.findByWithdrawalStatus(merchantId);
        BigDecimal successfulTransactions = totalSuccessfulTransactions.stream()
                .map(x -> x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal successfulSettlements = totalTransactionsSettled.stream()
                .map(x -> x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal successfulWithdrawals = totalWithdrawals.stream()
                .map(x -> x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allWithdrawals = successfulWithdrawals.add(successfulSettlements);
        BigDecimal merchantWalBal = successfulTransactions.subtract(allWithdrawals);
        if ((merchantWalBal != null) && (merchantWalBal.doubleValue() >= 0)) {
            return new PaymentGatewayResponse(Constant.OPERATION_SUCCESS, merchantWalBal);
        } else {
            return new PaymentGatewayResponse(Constant.ERROR_PROCESSING, null);
        }
    }
    
    @Override
    public PaymentGatewayResponse withdrawFromWallet(HttpServletRequest request, WayaWalletWithdrawal wayaWalletWithdrawal, String token) {
        @NotNull
        final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(wayaWalletWithdrawal.getMerchantId(), true);
        
        MerchantResponse merchant = null;
        Withdrawals withdrawals = null;
        WithdrawalRequest withdrawalRequest = null;
        String strLong = Utility.transactionId();
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, wayaWalletWithdrawal.getMerchantId());
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new PaymentGatewayResponse("Profile doesn't exist", HttpStatus.NOT_FOUND);
            }
            PinResponse pinResponse = authProxy.validatePin(merchant.getData().getUserId(), Long.valueOf(wayaWalletWithdrawal.getTransactionPin()), token);
            log.info("Pin Validation Response ::::" + pinResponse);
            if (!pinResponse.isStatus()) {
                return new PaymentGatewayResponse(Constant.INVALID_TRANSACTION_PIN, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
        
        DefaultWalletResponse defaultWalletResponse = walletProxy.getUserDefaultWalletAccount(token, merchant.getData().getUserId());
        log.info("Default Wallet Response::::"+ defaultWalletResponse);
//        double walletBal = defaultWalletResponse.getData().getClrBalAmt();
        log.info(" Wallet Data::::"+ defaultWalletResponse.getData());
        log.info(" Wallet Bal::::"+ defaultWalletResponse.getData().getClrBalAmt());
        log.info(" Amount To Withdraw ::::"+ wayaWalletWithdrawal.getAmount());
        if(wayaWalletWithdrawal.getAmount() <= defaultWalletResponse.getData().getClrBalAmt()) {
            log.info(" Got here 1::::");
            withdrawalRequest.setAmount(wayaWalletWithdrawal.getAmount());
            withdrawalRequest.setNarration("WayaQuick Credit To Customer's Account");
            withdrawalRequest.setBankCode(wayaWalletWithdrawal.getBankCode());
            withdrawalRequest.setBankName(wayaWalletWithdrawal.getBankName());
            withdrawalRequest.setCrAccount(wayaWalletWithdrawal.getAccountNo());
            log.info(" Got here 2::::");
            withdrawalRequest.setCrAccountName(wayaWalletWithdrawal.getAccountName());
            withdrawalRequest.setSaveBen(false);
            withdrawalRequest.setTransactionPin(wayaWalletWithdrawal.getTransactionPin());
            withdrawalRequest.setUserId(String.valueOf(merchant.getData().getUserId()));
            if (defaultWalletResponse.getStatus() == true) {
                log.info(" Got here 3::::");
                withdrawalRequest.setWalletAccountNo(defaultWalletResponse.getData().getAccountNo());
            } else {
                return new PaymentGatewayResponse(false, Constant.UNABLE_TO_FETCH_CREDIT_ACCOUNT_NUMBER, null);
            }
            log.info("Withdraw Wallet Req::::" + withdrawalRequest);
            WithdrawalResponse resp = withdrawalProxy.withdrawFromWallet(token, withdrawalRequest);
            log.info("Withdraw Wallet Response::::" + resp);
            MathContext mc = new MathContext(5);
            BigDecimal newAmount;
            
            newAmount = new BigDecimal(wayaWalletWithdrawal.getAmount(), mc);
            if (resp.isStatus()) {
                withdrawals.setWithdrawalStatus(WithdrawalStatus.SUSSESSFUL);
                withdrawals.setAmount(newAmount);
                withdrawals.setWithdrawalReferenceId(strLong);
                withdrawals.setCreatedBy(merchant.getData().getUserId());
                withdrawals.setDateCreated(LocalDateTime.now());
                withdrawals.setMerchantId(wayaWalletWithdrawal.getMerchantId());
                withdrawals.setMerchantUserId(merchant.getData().getUserId());
                withdrawalRepository.save(withdrawals);
                return new PaymentGatewayResponse(true, Constant.OPERATION_SUCCESS, resp);
            } else {
                withdrawals.setWithdrawalStatus(WithdrawalStatus.FAILED);
                withdrawals.setAmount(newAmount);
                withdrawals.setCreatedBy(merchant.getData().getUserId());
                withdrawals.setDateCreated(LocalDateTime.now());
                withdrawals.setWithdrawalReferenceId(strLong);
                withdrawals.setMerchantId(wayaWalletWithdrawal.getMerchantId());
                withdrawals.setMerchantUserId(merchant.getData().getUserId());
                return new PaymentGatewayResponse(false, Constant.ERROR_PROCESSING, resp);
            }
        }else{
            log.info(" Got here Insufficient::::");
            return new PaymentGatewayResponse(false, Constant.INSUFFICIENT_FUNDS, null);
        }
    }
    
    @Override
    public PaymentGatewayResponse adminWithdrawFromWallet(HttpServletRequest request, AdminWayaWithdrawal wayaWalletWithdrawal, String token) {
        MerchantResponse merchant = null;
        Withdrawals withdrawals = null;
        WithdrawalRequest withdrawalRequest = null;
        Date dte = new Date();
        String strLong = Long.toString(dte.getTime()) + rnd.nextInt(999999);
        TokenCheckResponse auth = getUserDataService.getUserData(token);
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, wayaWalletWithdrawal.getMerchantId());
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new PaymentGatewayResponse("Profile doesn't exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
        
        DefaultWalletResponse defaultWalletResponse = walletProxy.getUserDefaultWalletAccount(token, merchant.getData().getUserId());
        double walletBal = defaultWalletResponse.getData().getClrBalAmt();
        if (Double.valueOf(wayaWalletWithdrawal.getAmount()) <= walletBal) {
            withdrawalRequest.setAmount(wayaWalletWithdrawal.getAmount());
            withdrawalRequest.setNarration("WayaQuick Credit To Customer's Account");
            withdrawalRequest.setBankCode(wayaWalletWithdrawal.getBankCode());
            withdrawalRequest.setBankName(wayaWalletWithdrawal.getBankName());
            withdrawalRequest.setCrAccount(wayaWalletWithdrawal.getAccountNo());
            withdrawalRequest.setCrAccountName(wayaWalletWithdrawal.getAccountName());
            withdrawalRequest.setSaveBen(false);
            withdrawalRequest.setTransactionPin(strLong);
            withdrawalRequest.setUserId(String.valueOf(merchant.getData().getUserId()));
            if (defaultWalletResponse.getStatus() == true) {
                withdrawalRequest.setWalletAccountNo(defaultWalletResponse.getData().getAccountNo());
            } else {
                return new PaymentGatewayResponse(Constant.UNABLE_TO_FETCH_CREDIT_ACCOUNT_NUMBER, HttpStatus.NOT_FOUND);
            }
            
            WithdrawalResponse resp = withdrawalProxy.withdrawFromWallet(token, withdrawalRequest);
            MathContext mc = new MathContext(5);
            BigDecimal newAmount;
            
            newAmount = new BigDecimal(wayaWalletWithdrawal.getAmount(), mc);
            if (resp.isStatus()) {
                withdrawals.setWithdrawalStatus(WithdrawalStatus.SUSSESSFUL);
                withdrawals.setAmount(newAmount);
                withdrawals.setCreatedBy(auth.getData().getId());
                withdrawals.setDateCreated(LocalDateTime.now());
                withdrawals.setWithdrawalReferenceId(strLong);
                withdrawals.setMerchantId(wayaWalletWithdrawal.getMerchantId());
                withdrawals.setMerchantUserId(merchant.getData().getUserId());
                withdrawalRepository.save(withdrawals);
                return new PaymentGatewayResponse(Constant.OPERATION_SUCCESS, resp);
            } else {
                withdrawals.setWithdrawalStatus(WithdrawalStatus.FAILED);
                withdrawals.setAmount(newAmount);
                withdrawals.setCreatedBy(auth.getData().getId());
                withdrawals.setDateCreated(LocalDateTime.now());
                withdrawals.setWithdrawalReferenceId(strLong);
                withdrawals.setMerchantId(wayaWalletWithdrawal.getMerchantId());
                withdrawals.setMerchantUserId(merchant.getData().getUserId());
                return new PaymentGatewayResponse(Constant.ERROR_PROCESSING, resp);
            }
        } else {
            return new PaymentGatewayResponse(Constant.ERROR_PROCESSING, Constant.INSUFFICIENT_FUNDS);
        }
    }
    
    @Override
    public PaymentGatewayResponse getMerchantAccounts(String token, String merchantId) {
        MerchantResponse merchant = null;
        WalletResponse walletResponse = null;
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, merchantId);
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new PaymentGatewayResponse("Profile doesn't exist", HttpStatus.NOT_FOUND);
            }
            
            walletResponse = walletProxy.getWalletDetails(token, merchant.getData().getUserId());
            log.info("Wallet  Response ::::" + walletResponse);
            if (!walletResponse.getStatus() == true) {
                return new PaymentGatewayResponse(Constant.INVALID_TRANSACTION_PIN, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
        return new PaymentGatewayResponse(Constant.ERROR_PROCESSING, walletResponse);
    }

    // s-l done
    @Override
    public ResponseEntity<?> getTransactionByRef(HttpServletRequest req, String refNo) {
        PaymentGateway mPay = null;
        SandboxPaymentGateway msPay = null;
        TransactionStatusResponse response = null;
        try {
            if (refNo.startsWith("7263269")) { // sandbox payment
                msPay = sandboxPaymentGatewayRepo.findByRefNo(refNo).orElse(null);
                if (msPay == null) {
                    return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH SANDBOX TRANSACTION"), HttpStatus.BAD_REQUEST);
                }
                Customer customer = new Customer(msPay.getCustomerName(), msPay.getCustomerEmail(), msPay.getCustomerPhone(), msPay.getCustomerId());
                response = new TransactionStatusResponse(msPay.getRefNo(), msPay.getAmount(), msPay.getDescription(),
                        msPay.getFee(), msPay.getCurrencyCode(), msPay.getStatus().name(), msPay.getChannel().name(),
                        msPay.getMerchantName(), customer, msPay.getMerchantId(), msPay.getTranDate());
            } else { // live payment
                mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
                if (mPay == null) {
                    return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH LIVE TRANSACTION"), HttpStatus.BAD_REQUEST);
                }
                Customer customer = new Customer(mPay.getCustomerName(), mPay.getCustomerEmail(), mPay.getCustomerPhone(), mPay.getCustomerId());
                response = new TransactionStatusResponse(mPay.getRefNo(), mPay.getAmount(), mPay.getDescription(),
                        mPay.getFee(), mPay.getCurrencyCode(), mPay.getStatus().name(), mPay.getChannel().name(),
                        mPay.getMerchantName(), customer, mPay.getMerchantId(), mPay.getTranDate());
            }
        } catch (Exception e) {
            log.info("---------||||ERROR||||---------", e);
        }
        return new ResponseEntity<>(new SuccessResponse("Transaction Query", response), HttpStatus.OK);
    }

    // s-l done
    //TODO: Protect this method to check is user has access to operate Payment gateway
    @Override
    public ResponseEntity<?> abandonTransaction(HttpServletRequest request, String refNo, WayaPaymentStatus pay) {
        if (refNo.startsWith("7263269")) { // sandbox payment
            SandboxPaymentGateway msPay = sandboxPaymentGatewayRepo.findByRefNo(refNo).orElse(null);
            if (msPay == null) {
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
            }
            if (msPay.getStatus() != TransactionStatus.SUCCESSFUL) {
                msPay.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.ABANDONED);
                sandboxPaymentGatewayRepo.save(msPay);
            }
        } else {
            PaymentGateway mPay = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
            if (mPay == null) {
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
            }
            if (mPay.getStatus() != TransactionStatus.SUCCESSFUL) {
                mPay.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.ABANDONED);
                paymentGatewayRepo.save(mPay);
            }
        }
        return new ResponseEntity<>(new SuccessResponse("Updated", "Success Updated"), HttpStatus.OK);
    }

    // s-l done
    @Override
    public ResponseEntity<?> getMerchantTransactionRevenue(HttpServletRequest req, String merchantId, String token) {
        @NotNull
        final String merchantIdToUse = PaymentGateWayCommonUtils.getMerchantIdToUse(merchantId, true);
        
        MerchantResponse merchant = null;
        String mode = null;
        Long roleId;
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, merchantId);
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
            }
            mode = merchant.getData().getMerchantKeyMode();
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchant.getData().getUserId(), token);
//        if(response.getPermissions().contains(MerchantPermissions.CAN_VIEW_DASHBOARD_OVERVIEW)) {
        TransactionReportStats revenue = wayaPayment.getTransactionReportStats(merchantIdToUse, mode);
        return new ResponseEntity<>(new SuccessResponse("GET REVENUE", revenue), HttpStatus.OK);
//        }else{
//            return  new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    // s-l done
    @Override
    public ResponseEntity<?> getAllTransactionRevenue(HttpServletRequest req) {
        if (!PaymentGateWayCommonUtils.getAuthenticatedUser().getAdmin()) {
            throw new ApplicationException(403, "01", "Oops! Operation not allowed.");
        }
        List<TransactionReportStats> revenue = wayaPayment.getTransactionRevenueStats();
        return new ResponseEntity<>(new SuccessResponse("LIST REVENUE", revenue), HttpStatus.OK);
    }

    // s-l done
    @Override
    public ResponseEntity<?> updatePaymentStatus(WayaCallbackRequest requests) {
        // find in live
        PaymentGateway payment = paymentGatewayRepo.findByTranId(requests.getTrxId()).orElse(null);
        if (payment != null) {
            preprocessTransactionStatus(payment);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(wayapayStatusURL)).build();
        }
        // find in sandbox
        SandboxPaymentGateway sandboxPayment = sandboxPaymentGatewayRepo.findByTranId(requests.getTrxId()).orElse(null);
        if (sandboxPayment != null) {
            preprocessSandboxTransactionStatus(sandboxPayment);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(wayapayStatusURL)).build();
        }
        
        return ResponseEntity.badRequest().body("Ooops! TRANSACTION DOES NOT EXIST... FAILED TO COMPLETE TRANSACTION.");
    }

    // s-l done
    @Override
    public ResponseEntity<?> updatePaymentStatus(String refNo) {
        if (refNo.startsWith("7263269")) {
            SandboxPaymentGateway sandboxPayment = sandboxPaymentGatewayRepo.findByRefNo(refNo).orElse(null);
            if (sandboxPayment == null) {
                return ResponseEntity.badRequest().body("UNKNOWN SANDBOX PAYMENT TRANSACTION STATUS");
            }
            preprocessSandboxTransactionStatus(sandboxPayment);
        } else {
            PaymentGateway payment = paymentGatewayRepo.findByRefNo(refNo).orElse(null);
            if (payment == null) {
                return ResponseEntity.badRequest().body("UNKNOWN PAYMENT TRANSACTION STATUS");
            }
            preprocessTransactionStatus(payment);
        }
        return ResponseEntity.ok().body("Transaction status updated successful");
    }

    // s-l done
    @Override
    public ResponseEntity<PaymentGatewayResponse> filterSearchCustomerTransactions(QueryCustomerTransactionPojo queryPojo, Pageable pageable) {
        AuthenticatedUser authenticatedUser = PaymentGateWayCommonUtils.getAuthenticatedUser();
        String token = paymentGateWayCommonUtils.getDaemonAuthToken();
        MerchantData merchantResponse = merchantProxy.getMerchantInfo(token, authenticatedUser.getMerchantId()).getData();
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchantResponse.getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_TRANSACTIONS)) {
        queryPojo.setMerchantId(merchantResponse.getMerchantId());
        return new ResponseEntity<>(new SuccessResponse("Data fetched successfully",
                getCustomerTransaction(queryPojo, merchantResponse.getMerchantKeyMode(), pageable)), HttpStatus.OK);
//        }else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    // s-l done
    @Override
    public Page<?> getCustomerTransaction(QueryCustomerTransactionPojo queryPojo, String mode, Pageable pageable) {
        Page<?> result;
        String merchantId = queryPojo.getMerchantId();
        if (mode.equals(MerchantTransactionMode.PRODUCTION.toString())) {
            if (ObjectUtils.isNotEmpty(queryPojo.getStatus()) && ObjectUtils.isNotEmpty(queryPojo.getChannel())) {
                result = paymentGatewayRepo.findByCustomerIdChannelStatus(
                        queryPojo.getCustomerId(), merchantId,
                        queryPojo.getStatus().name(), queryPojo.getChannel().name(), pageable);
            } else if (ObjectUtils.isNotEmpty(queryPojo.getChannel())) {
                result = paymentGatewayRepo.findByCustomerIdChannel(queryPojo.getCustomerId(), merchantId, queryPojo.getChannel().name(), pageable);
            } else if (ObjectUtils.isNotEmpty(queryPojo.getStatus())) {
                result = paymentGatewayRepo.findByStatus(queryPojo.getCustomerId(), merchantId, queryPojo.getStatus().name(), pageable);
            } else {
                result = paymentGatewayRepo.findByCustomerId(queryPojo.getCustomerId(), merchantId, pageable);
            }
        } else {
            if (ObjectUtils.isNotEmpty(queryPojo.getStatus()) && ObjectUtils.isNotEmpty(queryPojo.getChannel())) {
                result = sandboxPaymentGatewayRepo.findByCustomerIdChannelStatus(
                        queryPojo.getCustomerId(), merchantId,
                        queryPojo.getStatus().name(), queryPojo.getChannel().name(), pageable);
            } else if (ObjectUtils.isNotEmpty(queryPojo.getChannel())) {
                result = sandboxPaymentGatewayRepo.findByCustomerIdChannel(queryPojo.getCustomerId(), merchantId, queryPojo.getChannel().name(), pageable);
            } else if (ObjectUtils.isNotEmpty(queryPojo.getStatus())) {
                result = sandboxPaymentGatewayRepo.findByStatus(queryPojo.getCustomerId(), merchantId, queryPojo.getStatus().name(), pageable);
            } else {
                result = sandboxPaymentGatewayRepo.findByCustomerId(queryPojo.getCustomerId(), merchantId, pageable);
            }
        }
        return result;
    }

    // s-l done SANDBOX Counterpart: preprocessSandboxTransactionStatus()
    private void preprocessTransactionStatus(PaymentGateway payment) {
        try {
            WayaTransactionQuery response = uniPaymentProxy.transactionQuery(payment.getTranId(), MerchantTransactionMode.PRODUCTION.name());
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
                // send email and in-app notification (will only be sent if successful)
                paymemtGatewayEntityListener.sendTransactionNotificationAfterPaymentIsSuccessful(payment);
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR||||-------", e);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED);
            paymentGatewayRepo.save(payment);
        }
    }

    // s-l done
    private void preprocessSandboxTransactionStatus(SandboxPaymentGateway payment) {
        try {
            WayaTransactionQuery response = uniPaymentProxy.transactionQuery(payment.getTranId(), MerchantTransactionMode.TEST.name());
            log.info("-----UNIFIED PAYMENT RESPONSE {}----------", response);
            if (ObjectUtils.isNotEmpty(response)) {
                if (ObjectUtils.isNotEmpty(response.getStatus()) && response.getStatus().toUpperCase().equals(TStatus.APPROVED.name())) {
                    payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL);
                    payment.setSuccessfailure(true);
                    payment.setTranId(response.getOrderId());
                    payment.setProcessingFee(new BigDecimal(response.getConvenienceFee()));
                    if (payment.getIsFromRecurrentPayment()) {
                        updateSandboxRecurrentTransaction(payment);
                    }
                } else {
                    com.wayapaychat.paymentgateway.enumm.TransactionStatus transactionStatus = Arrays.stream(com.wayapaychat.paymentgateway.enumm.TransactionStatus.values()).map(Enum::name)
                            .collect(Collectors.toList())
                            .contains(response.getStatus().toUpperCase()) ? com.wayapaychat.paymentgateway.enumm.TransactionStatus.valueOf(response.getStatus().toUpperCase()) : com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED;
                    payment.setStatus(transactionStatus);
                    payment.setSuccessfailure(false);
                    payment.setTranId(response.getOrderId());
                }
                sandboxPaymentGatewayRepo.save(payment);
            }
        } catch (Exception e) {
            log.error("------||||SYSTEM ERROR||||-------", e);
            payment.setStatus(com.wayapaychat.paymentgateway.enumm.TransactionStatus.FAILED);
            sandboxPaymentGatewayRepo.save(payment);
        }
    }

    // s-l done SANDBOX Counterpart: updateSandboxRecurrentTransaction()
    @Override
    public void updateRecurrentTransaction(@NotNull final PaymentGateway paymentGateway) {
        if (paymentGateway.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL) {
            Optional<RecurrentTransaction> optionalRecurrentTransaction = recurrentTransactionRepository.getByTransactionRef(paymentGateway.getRefNo());
            if (optionalRecurrentTransaction.isPresent()) {
                LocalDateTime date = LocalDateTime.now();
                RecurrentTransaction foundRecurrentTransaction = optionalRecurrentTransaction.get();
                LocalDateTime chargeDateAfterFirstPayment = foundRecurrentTransaction.getNextChargeDateAfterFirstPayment();
                if (foundRecurrentTransaction.getTotalChargeCount() == 0) {
                    foundRecurrentTransaction.setFirstPaymentDate(date);
                }
                Integer totalChargeCount = foundRecurrentTransaction.getTotalChargeCount() + 1;
                foundRecurrentTransaction.setModifiedBy(0L);
                foundRecurrentTransaction.setDateModified(date);
                foundRecurrentTransaction.setActive(true);
                foundRecurrentTransaction.setStatus(RecurrentPaymentStatus.ACTIVE_RENEWING);
                foundRecurrentTransaction.setLastChargeDate(date);
                foundRecurrentTransaction.setUpSessionId(paymentGateway.getSessionId());
                foundRecurrentTransaction.setTotalChargeCount(totalChargeCount);
                foundRecurrentTransaction.setNextChargeDate(ObjectUtils.isEmpty(chargeDateAfterFirstPayment)
                        ? date.plusDays(foundRecurrentTransaction.getInterval()) : chargeDateAfterFirstPayment);
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

    // s-l done
    @Override
    public void updateSandboxRecurrentTransaction(@NotNull final SandboxPaymentGateway paymentGateway) {
        if (paymentGateway.getStatus() == com.wayapaychat.paymentgateway.enumm.TransactionStatus.SUCCESSFUL) {
            Optional<SandboxRecurrentTransaction> optionalRecurrentTransaction = sandboxRecurrentTransactionRepository.getByTransactionRef(paymentGateway.getRefNo());
            if (optionalRecurrentTransaction.isPresent()) {
                LocalDateTime date = LocalDateTime.now();
                SandboxRecurrentTransaction foundRecurrentTransaction = optionalRecurrentTransaction.get();
                LocalDateTime chargeDateAfterFirstPayment = foundRecurrentTransaction.getNextChargeDateAfterFirstPayment();
                if (foundRecurrentTransaction.getTotalChargeCount() == 0) {
                    foundRecurrentTransaction.setFirstPaymentDate(date);
                }
                Integer totalChargeCount = foundRecurrentTransaction.getTotalChargeCount() + 1;
                foundRecurrentTransaction.setModifiedBy(0L);
                foundRecurrentTransaction.setDateModified(date);
                foundRecurrentTransaction.setActive(true);
                foundRecurrentTransaction.setStatus(RecurrentPaymentStatus.ACTIVE_RENEWING);
                foundRecurrentTransaction.setLastChargeDate(date);
                foundRecurrentTransaction.setUpSessionId(paymentGateway.getSessionId());
                foundRecurrentTransaction.setTotalChargeCount(totalChargeCount);
                foundRecurrentTransaction.setNextChargeDate(ObjectUtils.isEmpty(chargeDateAfterFirstPayment)
                        ? date.plusDays(foundRecurrentTransaction.getInterval()) : chargeDateAfterFirstPayment);
                sandboxRecurrentTransactionRepository.save(foundRecurrentTransaction);
                
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

    // s-l done
    @Override
    public ResponseEntity<PaymentGatewayResponse> getYearMonthTransactionStats(String merchantId, Long year, Date startDate, Date endDate, String token) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        String mode = MerchantTransactionMode.PRODUCTION.name();
        
        MerchantResponse merchant = null;
        if (ObjectUtils.isNotEmpty(merchantIdToUse)) {
            // get merchant data
            try {
                merchant = merchantProxy.getMerchantInfo(token, merchantIdToUse);
                if (!merchant.getCode().equals("00") || (merchant == null)) {
                    return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
                }
                mode = merchant.getData().getMerchantKeyMode();
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }
        }
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchant.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_TRANSACTIONS)) {
        List<TransactionYearMonthStats> transactionYearMonthStats = wayaPaymentDAO.getTransactionStatsByYearAndMonth(merchantIdToUse, year, startDate, endDate, mode);
        BigDecimal totalRevenueForSelectedDateRange = transactionYearMonthStats.stream()
                .map(TransactionYearMonthStats::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = Map.of("dateRangeResult", transactionYearMonthStats,
                "totalRevenueForSelectedDateRange", totalRevenueForSelectedDateRange);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, result), HttpStatus.OK);
//        }else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    // s-l done
    @Override
    public ResponseEntity<PaymentGatewayResponse> getTransactionOverviewStats(String merchantId, String token) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        String mode = MerchantTransactionMode.PRODUCTION.name();
        MerchantResponse merchant = null;
        if (ObjectUtils.isNotEmpty(merchantIdToUse)) {
            // get merchant data
            try {
                merchant = merchantProxy.getMerchantInfo(token, merchantIdToUse);
                if (!merchant.getCode().equals("00") || (merchant == null)) {
                    return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
                }
                mode = merchant.getData().getMerchantKeyMode();
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage().toString());
            }
        }
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchant.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_DASHBOARD_OVERVIEW)) {
        log.info("merchant to use is " + merchantIdToUse);
        TransactionOverviewResponse transactionOverviewResponse = wayaPaymentDAO.getTransactionReport(merchantIdToUse, mode);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, transactionOverviewResponse), HttpStatus.OK);
//        }
//        else{
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }

    // s-l done
    @Override
    public ResponseEntity<PaymentGatewayResponse> getTransactionGrossAndNetRevenue(String merchantId, String token) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        String mode = MerchantTransactionMode.PRODUCTION.name();
        
        MerchantResponse merchant = null;
        if (ObjectUtils.isNotEmpty(merchantIdToUse)) {
            // get merchant data
            try {
                merchant = merchantProxy.getMerchantInfo(token, merchantIdToUse);
                if (!merchant.getCode().equals("00") || (merchant == null)) {
                    return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
                }
                mode = merchant.getData().getMerchantKeyMode();
            } catch (Exception ex) {
                if (ex instanceof FeignException) {
                    String httpStatus = Integer.toString(((FeignException) ex).status());
                    log.error("Feign Exception Status {}", httpStatus);
                }
                log.error("Higher Wahala {}", ex.getMessage());
                log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
            }
        }
        
        TransactionRevenueStats transactionRevenueStats = wayaPaymentDAO.getTransactionGrossAndNetRevenue(merchantIdToUse, mode);
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, transactionRevenueStats), HttpStatus.OK);
    }

    // s-l done
    @Override
    public ResponseEntity<PaymentGatewayResponse> fetchPaymentLinkTransactions(String merchantId, String paymentLinkId, String token, Pageable pageable) {
        String merchantIdToUse = getMerchantIdToUse(merchantId, false);
        
        MerchantResponse merchant = null;
        // get merchant data
        try {
            merchant = merchantProxy.getMerchantInfo(token, merchantIdToUse);
            if (!merchant.getCode().equals("00") || (merchant == null)) {
                return new ResponseEntity<>(new SuccessResponse("Profile doesn't exist", null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            if (ex instanceof FeignException) {
                String httpStatus = Integer.toString(((FeignException) ex).status());
                log.error("Feign Exception Status {}", httpStatus);
            }
            log.error("Higher Wahala {}", ex.getMessage());
            log.error("PROFILE ERROR MESSAGE {}", ex.getLocalizedMessage());
        }
        
        Page<?> result = null;
//        RolePermissionResponsePayload response = roleProxy.fetchUserRoleAndPermissions(merchant.getData().getUserId(), token);
//        if (response.getPermissions().contains(MerchantPermissions.CAN_VIEW_DASHBOARD_OVERVIEW)) {
        if (merchant.getData().getMerchantKeyMode() == MerchantTransactionMode.PRODUCTION.toString()) {
            if (ObjectUtils.isEmpty(merchantIdToUse)) {
                result = paymentGatewayRepo.getAllByPaymentLinkId(paymentLinkId, pageable);
            } else {
                result = paymentGatewayRepo.getAllByPaymentLinkId(merchantIdToUse, paymentLinkId, pageable);
            }
        } else {
            if (ObjectUtils.isEmpty(merchantIdToUse)) {
                result = sandboxPaymentGatewayRepo.getAllByPaymentLinkId(paymentLinkId, pageable);
            } else {
                result = sandboxPaymentGatewayRepo.getAllByPaymentLinkId(merchantIdToUse, paymentLinkId, pageable);
            }
        }
        return new ResponseEntity<>(new SuccessResponse(DEFAULT_SUCCESS_MESSAGE, result), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(new SuccessResponse(Constant.PERMISSION_ERROR), HttpStatus.NOT_FOUND);
//        }
    }
    
    private String replaceKeyPrefixWithEmptyString(String pub) {
        return pub.contains("WAYA")
                ? pub.replace("WAYAPUBK_TEST_0x", "")
                : pub.replace("WAYAPUBK_PROD_0x", "");
    }

    //REGION : LOCAL , INTERNATIONAL
    private BigDecimal calculateWayapayFee(
            String merchantId, BigDecimal amount, ProductName productName, String region) {
        MerchantProductPricingQuery merchantProductPricingQuery = MerchantProductPricingQuery
                .builder()
                .merchantId(merchantId)
                .productName(productName)
                .build();
        log.info("PRODUCT NAME ::: " + productName + "REGION " + region);
        log.info("TOKEN ::: " + DAEMON_TOKEN + "  MERCHANT_ID " + merchantId);
        MerchantProductPricingResponse merchantProductPricingResponse = iSettlementProductPricingProxy.getMerchantProductPricing(
                merchantProductPricingQuery.getMerchantId(), merchantProductPricingQuery.getProductName(), DAEMON_TOKEN
        );
        ProductPricingResponse productPricingResponse = merchantProductPricingResponse.getData();
        log.info("-------MERCHANT PRODUCT PRICING {}--------", productPricingResponse);
        Double feePercentage = 0D;
        if (ObjectUtils.isEmpty(productPricingResponse)) {
            throw new ApplicationException(400, "product_pricing_not_found", "Merchant Product pricing not found");
        }
        if (productPricingResponse.getLocalDiscountRate() > 0) {
            feePercentage = productPricingResponse.getLocalDiscountRate();
        }
        if (productPricingResponse.getLocalRate() > 0) {
            feePercentage += productPricingResponse.getLocalRate();
        }
        BigDecimal fee = amount.multiply(new BigDecimal(feePercentage)).divide(new BigDecimal(100), MathContext.DECIMAL64);
        if ((productName.equals(ProductName.WALLET) || productName.equals(ProductName.CARD) || productName.equals(ProductName.BANK)
                || productName.equals(ProductName.USSD)) || productName.equals(ProductName.PAYATTITUDE) && region.equals("LOCAL")) {
            BigDecimal cappedFee = productPricingResponse.getLocalProcessingFeeCappedAt();
            if (fee.compareTo(cappedFee) > 0) {
                return cappedFee;
            }
        }
        return fee;
    }
    
    @Override
    public ResponseEntity<?> tokenizeCard(CardTokenization cardTokenization, String token) {
        try {

            //check if card has been tokenized
//            Optional<TokenizedCard> getCard = tokenizedRepo.findByCustomerIdAndCardNumber(
//                    cardTokenization.getCustomerId(), cardTokenization.getPan());
//            if(getCard.isPresent()){
//                return new ResponseEntity<>("Existing tokenized card", HttpStatus.FOUND);
//            }
            //send request to tokenize card
//            TokenizationResponse tokenize = iswService.tokenizeCard(cardTokenization);
//            if (tokenize.getToken() != null || !tokenize.getToken().equalsIgnoreCase("")) {
//                TokenizedCard card = new TokenizedCard();
//                card.setMerchantId(cardTokenization.getMerchantId());
//                card.setCustomerId(cardTokenization.getCustomerId());
//                card.setCardToken(tokenize.getToken());
//                card.setDateCreated(LocalDateTime.now());
//                card.setCardTokenReference(tokenize.getTransactionRef());
//                card.setEncryptedCard(cardTokenization.getPan());
//
//              TokenizedCard save = tokenizedRepo.save(card);
//                log.info("Tokenize card successful: ", tokenize);
//            }
            //dump response
            TokenizationResponse tokenize = new TokenizationResponse();
            tokenize.setBalance("0.00");
            tokenize.setCardType("");
            tokenize.setPanLast4Digits("0002");
            tokenize.setToken("5123453847245380");
            tokenize.setTokenExpiryDate(cardTokenization.getExpiryDate());
            tokenize.setTransactionRef(cardTokenization.getTransactionRef());
            log.error("Tokenize card failed: ", tokenize);
            return new ResponseEntity<>(tokenize, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Exception: ", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
    
    @Override
    public ResponseEntity<?> tokenizePayment(String customerId, String merchantId, String transactionRef,
            String cardToken, String token) {
        try {
            //get merchant info
            MerchantResponse merchant = merchantProxy.getMerchantInfo(token, merchantId);
            if (merchant == null || !merchant.getCode().equals("00")) {
                return new ResponseEntity<>("Merchant id doesn't exist", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            //check that the transaction ref provided exist
            WayaTransactionQuery response = null;
            String mode = "";
            if (transactionRef.startsWith("7263269")) {
                mode = MerchantTransactionMode.TEST.name();
            } else {
                mode = MerchantTransactionMode.PRODUCTION.name();
            }
            response = uniPaymentProxy.transactionQuery(transactionRef, mode);
            if (response == null) {
                return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH"), HttpStatus.BAD_REQUEST);
            }

            //validate token against customer and merchant
            Optional<TokenizedCard> validateToken = tokenizedRepo.findByRefMerchant(customerId, merchantId);
            if (validateToken.isEmpty()) {
                return new ResponseEntity<>(new ErrorResponse("No Valid Token for customer"), HttpStatus.BAD_REQUEST);
            }
            TokenizedCard isTokenValid = validateToken.get();
            if (!isTokenValid.getCardToken().equalsIgnoreCase(cardToken)) {
                return new ResponseEntity<>(new ErrorResponse("No Valid Token for customer"), HttpStatus.BAD_REQUEST);
            }
            //send request to pay with token
            TokenizePayment pay = new TokenizePayment();
            pay.setAmount(response.getAmount());
            pay.setCurrency("NGN");
            pay.setToken(cardToken);
            pay.setTransactionRef(transactionRef);
            pay.setCustomerId(customerId);
            pay.setTokenExpiryDate("");
            
            TokenizationResponse tokenPayment = iswService.tokenPayment(pay);
            return new ResponseEntity<>(tokenPayment, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
    
    @Override
    public ResponseEntity<?> chargeWithToken(String customerId, String transactionRef, String cardToken, String amount, String token) {
        //mock response
        TokenizePaymentResponse tokenPayment = new TokenizePaymentResponse();
        tokenPayment.setAmount(amount);
        tokenPayment.setMessage("Charge Successful");
        tokenPayment.setTransactionRef(transactionRef);
        return new ResponseEntity<>(tokenPayment, HttpStatus.CREATED);
    }
}
