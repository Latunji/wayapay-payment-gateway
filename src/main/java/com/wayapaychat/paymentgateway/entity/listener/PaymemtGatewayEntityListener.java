package com.wayapaychat.paymentgateway.entity.listener;


import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import com.wayapaychat.paymentgateway.common.utils.VariableUtil;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.*;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.LitePaymentGateway;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.kafkamessagebroker.producer.IkafkaMessageProducer;
import com.wayapaychat.paymentgateway.pojo.notification.*;
import com.wayapaychat.paymentgateway.pojo.waya.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.waya.NotificationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentData;
import com.wayapaychat.paymentgateway.pojo.waya.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TransactionStatusResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.IdentityManagementServiceProxy;
import com.wayapaychat.paymentgateway.proxy.NotificationServiceProxy;
import com.wayapaychat.paymentgateway.proxy.WebhookPushClient;
import com.wayapaychat.paymentgateway.repository.PaymentGatewayRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PaymemtGatewayEntityListener {
    private static final String CURRENCY_DISPLAY = "NGN";
    private static NotificationServiceProxy notificationServiceProxy;
    private static AuthApiClient authApiClient;
    private static VariableUtil variableUtil;
    private static IkafkaMessageProducer ikafkaMessageProducer;
    private static ModelMapper modelMapper;
    private static PaymentGatewayRepository paymentGatewayRepository;
    private static IdentityManagementServiceProxy identityManagementServiceProxy;
    private static PaymentGateWayCommonUtils paymentGateWayCommonUtils;

    @Value("${service.token}")
    public String daemonToken;

    private static String getDaemonAuthToken() throws Exception {
        TokenAuthResponse authToken = authApiClient.authenticateUser(
                LoginRequest.builder()
                        .password(variableUtil.getPassword())
                        .emailOrPhoneNumber(variableUtil.getUserName())
                        .build());
        log.info("AUTHENTICATION RESPONSE: " + authToken.toString());
        if (!authToken.getStatus()) {
            log.info("------||||FAILED TO AUTHENTICATE DAEMON USER [email: {} , password: {}]||||--------",
                    variableUtil.getUserName(), variableUtil.getPassword());
            throw new Exception("Failed to process user authentication...!");
        }
        PaymentData payData = authToken.getData();
        return payData.getToken();
    }

    

    @Autowired
    public void setIdentityManagementServiceProxy(IdentityManagementServiceProxy identityManagementServiceProxy) {
        PaymemtGatewayEntityListener.identityManagementServiceProxy = identityManagementServiceProxy;
        log.info("Initializing with dependency [" + identityManagementServiceProxy + "]");
    }

    @Autowired
    public void setAuthApiClient(AuthApiClient authApiClient) {
        PaymemtGatewayEntityListener.authApiClient = authApiClient;
        log.info("Initializing with dependency [" + authApiClient + "]");
    }

    @Autowired
    public void setNotificationServiceProxy(NotificationServiceProxy notificationServiceProxy) {
        PaymemtGatewayEntityListener.notificationServiceProxy = notificationServiceProxy;
        log.info("Initializing with dependency [" + notificationServiceProxy + "]");
    }

    @Autowired
    public void setVariableUtil(VariableUtil variableUtil) {
        PaymemtGatewayEntityListener.variableUtil = variableUtil;
        log.info("Initializing with dependency [" + variableUtil + "]");
    }

    @Autowired
    public void setIkafkaMessageProducer(IkafkaMessageProducer ikafkaMessageProducer) {
        PaymemtGatewayEntityListener.ikafkaMessageProducer = ikafkaMessageProducer;
        log.info("Initializing with dependency [" + ikafkaMessageProducer + "]");
    }

    @Autowired
    public void setIkafkaMessageProducer(ModelMapper modelMapper) {
        PaymemtGatewayEntityListener.modelMapper = modelMapper;
        log.info("Initializing with dependency [" + modelMapper + "]");
    }

    @Autowired
    public void setIkafkaMessageProducer(PaymentGatewayRepository paymentGatewayRepository) {
        PaymemtGatewayEntityListener.paymentGatewayRepository = paymentGatewayRepository;
        log.info("Initializing with dependency [" + paymentGatewayRepository + "]");
    }

    //    @PostPersist
//    @PostUpdate
    public void sendTransactionNotificationAfterPaymentIsSuccessful(PaymentGateway paymentGateway) {
        CompletableFuture.runAsync(() -> {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            log.info("------||||PREPROCESSING TRANSACTION BEFORE SENDING NOTIFICATION WITH TRANSACTION ID: {}||||--------", paymentGateway.getTranId());

            String token = null;
            MerchantData merchantData = new MerchantData();
            try {
                token = getDaemonAuthToken();
                MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(token, paymentGateway.getMerchantId());
                merchantData = merchantResponse.getData();
            } catch (Exception e) {
            }


            if (paymentGateway.getStatus() == TransactionStatus.SUCCESSFUL) {
                NotificationPojo notificationPojo = NotificationPojo
                        .builder()
                        .paymentChannel(paymentGateway.getChannel())
                        .merchantName(paymentGateway.getMerchantName())
                        .transactionDate(paymentGateway.getVendorDate())
                        .transactionAmount(paymentGateway.getAmount())
                        .transactionMode(paymentGateway.getMode().toString())
                        .customerEmailAddress(paymentGateway.getCustomerEmail())
                        .customerName(paymentGateway.getCustomerName())
                        .merchantName(paymentGateway.getMerchantName())
                        .merchantEmailAddress(paymentGateway.getMerchantEmail())
                        .paymentGatewayTransactionId(paymentGateway.getTranId())
                        .channelTransactionId(paymentGateway.getRefNo())
                        .currency(getCurrencyName(paymentGateway.getCurrencyCode()))
                        .updatedAt(paymentGateway.getVendorDate() == null ? paymentGateway.getTranDate() : paymentGateway.getVendorDate())
                        .build();
                try {
                    processEmailAlert(notificationPojo);
                    processInAppNotification("TRANSACTION",
                            merchantData.getUserId(),
                            String.format("You received payment of %s from %s", paymentGateway.getAmount(), paymentGateway.getCustomerName()),
                            "1",
                            token
                    );
                } catch (Exception e) {
                    log.error("{0}", e);
                }
            }
        });

    }

    private void processEmailAlert(NotificationPojo notificationPojo) throws Exception {
        NotificationStreamData notificationStreamData = NotificationStreamData.builder().build();
        notificationStreamData.setMessage("A transaction has successfully occurred");

        EmailStreamData emailStreamData = EmailStreamData.builder().build();
        emailStreamData.setInitiator(1 + "");
        emailStreamData.setEventCategory(EventCategory.TRANSACTION);
        emailStreamData.setEventType(EventType.EMAIL);
        emailStreamData.setProductType(ProductType.WAYAPAY);
        emailStreamData.setTransactionId(notificationPojo.getChannelTransactionId());
        emailStreamData.setPaymentChannel(notificationPojo.getPaymentChannel());
        emailStreamData.setNarration(notificationPojo.getTransactionNarration());
        emailStreamData.setNarration("Transaction successful");
        emailStreamData.setAmount(notificationPojo.getTransactionAmount().toString());
        emailStreamData.setTransactionDate(notificationPojo.getUpdatedAt().toString());
        emailStreamData.setMode(variableUtil.getMode());
        emailStreamData.setCustomerName(notificationPojo.getCustomerName());
        emailStreamData.setMerchantName(notificationPojo.getMerchantName());

        notificationStreamData.setNames(List.of(NotificationReceiver.builder()
                .email(notificationPojo.getCustomerEmailAddress())
                .fullName(notificationPojo.getCustomerName())
                .build()));
        emailStreamData.setData(notificationStreamData);
        emailStreamData.setForMerchant(false + "");
        NotificationServiceResponse notificationServiceResponse = notificationServiceProxy.sendEmailNotificationTransaction(emailStreamData, getDaemonAuthToken());
        log.info("------||||NOTIFICATION HAS BEEN SENT TO CUSTOMER EMAIL ADDRESS {}||||--------", notificationPojo.getCustomerEmailAddress());
        log.info("------||||NOTIFICATION SERVICE RESPONSE {}||||--------", notificationServiceResponse);

        notificationStreamData.setNames(List.of(NotificationReceiver.builder()
                .email(notificationPojo.getMerchantEmailAddress())
                .fullName(notificationPojo.getMerchantName())
                .build()));
        emailStreamData.setData(notificationStreamData);
        emailStreamData.setForMerchant(true + "");
        NotificationServiceResponse notificationServiceResponse1 = notificationServiceProxy.sendEmailNotificationTransaction(emailStreamData, getDaemonAuthToken());
        log.info("------||||NOTIFICATION HAS BEEN SENT TO MERCHANT EMAIL ADDRESS {}||||--------", notificationPojo.getMerchantEmailAddress());
        log.info("------||||NOTIFICATION SERVICE RESPONSE {}||||--------", notificationServiceResponse1);
    }

    private void processInAppNotification(String type, final Long receiverUserId, String message, String senderUserId, String demonToken) throws Exception {
        CompletableFuture.runAsync(() -> {
            InAppNotificationEvent inAppNotificationEvent = InAppNotificationEvent.builder()
                    .data(InAppPayload.builder()
                            .type(type)
                            .users(List.of(InAppRecipient.builder()
                                    .userId(receiverUserId.toString())
                                    .build()))
                            .message(message)
                            .build())
                    .token(demonToken)
                    .eventType(EventType.IN_APP)
                    .build();
            inAppNotificationEvent.setInitiator(senderUserId);
            ResponseEntity<?> response = notificationServiceProxy.sendInAppNotification(inAppNotificationEvent, demonToken);
            log.info("--------||||IN APP NOTIFICATION RESPONSE||||---------\n{}", response);
        });
    }

    private String getCurrencyName(String currencyCode) {
        Optional<Currency> currency = Currency.getAvailableCurrencies().stream().filter(c -> (c.getNumericCode() + "").equals(currencyCode)).findAny();
        if (currency.isPresent())
            return currency.get().getCurrencyCode();
        return CURRENCY_DISPLAY;
    }

    //    @PostPersist
//    @PostUpdate
    public void sendTransactionForSettlement(PaymentGateway paymentGateway) {
        log.info("------||||PENDING SETTLEMENT PUBLISHED FOR PROCESSING||||--------");
        if (!paymentGateway.isSentForSettlement()) {
            if (Objects.equals(paymentGateway.getStatus(), TransactionStatus.SUCCESSFUL) &&
                    !Objects.equals(paymentGateway.getSettlementStatus(), SettlementStatus.SETTLED)) {
                LitePaymentGateway litePaymentGateway = new LitePaymentGateway();
                modelMapper.map(paymentGateway, litePaymentGateway);
                ProducerMessageDto producerMessageDto = ProducerMessageDto.builder()
                        .data(litePaymentGateway)
                        .eventCategory(EventType.PENDING_TRANSACTION_SETTLEMENT)
                        .build();
                ikafkaMessageProducer.send("merchant.settlement", producerMessageDto);
                paymentGateway.setSentForSettlement(true);
                paymentGatewayRepository.save(paymentGateway);
                log.info("------||||SUCCESSFULLY PUBLISHED PENDING SETTLEMENT FOR PROCESSING {}||||--------", producerMessageDto);
            }
        }
    }
  
    public void pushToMerchantWebhook(TransactionStatusResponse payment) {
        MerchantData merchantData = new MerchantData();
        try {
            log.info("Pushing to merchant webhook trnxid: {} merchantid: {}", payment.getOrderId(), payment.getMerchantId());
            MerchantResponse merchantResponse = identityManagementServiceProxy.getMerchantDetail(getDaemonAuthToken(), payment.getMerchantId());
            merchantData = merchantResponse.getData();
            WebhookPushClient.postObjectToUrl(payment, merchantData.getMerchantWebHookURL());
        } catch (Exception e) {
            log.error("Error pushing to webhook url {} error {}", merchantData, e);
        }   
    }

}
