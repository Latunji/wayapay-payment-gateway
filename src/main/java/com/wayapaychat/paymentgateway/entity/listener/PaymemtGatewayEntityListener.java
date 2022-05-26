package com.wayapaychat.paymentgateway.entity.listener;


import com.wayapaychat.paymentgateway.common.utils.VariableUtil;
import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.*;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.LitePaymentGatewayMessagePayload;
import com.wayapaychat.paymentgateway.kafkamessagebroker.model.ProducerMessageDto;
import com.wayapaychat.paymentgateway.kafkamessagebroker.producer.IkafkaMessageProducer;
import com.wayapaychat.paymentgateway.pojo.notification.EmailStreamData;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationReceiver;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationServiceResponse;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationStreamData;
import com.wayapaychat.paymentgateway.pojo.waya.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.waya.NotificationPojo;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentData;
import com.wayapaychat.paymentgateway.pojo.waya.TokenAuthResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.NotificationServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.persistence.PostPersist;
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

    //    @PostPersist
//    @PostUpdate
    public void sendTransactionNotificationAfterPaymentIsSuccessful(PaymentGateway paymentGateway) {
        CompletableFuture.runAsync(() -> {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            log.info("------||||PREPROCESSING TRANSACTION BEFORE SENDING NOTIFICATION WITH TRANSACTION ID: {}||||--------",
                    paymentGateway.getTranId());
            if (paymentGateway.getStatus() == TransactionStatus.SUCCESSFUL) {
                NotificationPojo notificationPojo = NotificationPojo
                        .builder()
                        .paymentChannel(paymentGateway.getChannel())
                        .merchantName(paymentGateway.getMerchantName())
                        .transactionDate(paymentGateway.getVendorDate())
                        .transactionAmount(paymentGateway.getAmount())
                        .transactionMode(variableUtil.getMode())
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

    private String getCurrencyName(String currencyCode) {
        Optional<Currency> currency = Currency.getAvailableCurrencies().stream().filter(c -> (c.getNumericCode() + "").equals(currencyCode)).findAny();
        if (currency.isPresent())
            return currency.get().getCurrencyCode();
        return CURRENCY_DISPLAY;
    }

    @PostPersist
    public void sendTransactionForSettlement(PaymentGateway paymentGateway) {
        log.info("------||||PENDING SETTLEMENT PUBLISHED FOR PROCESSING||||--------");
        if (!Objects.equals(paymentGateway.getSettlementStatus(), SettlementStatus.SETTLED)) {
            LitePaymentGatewayMessagePayload litePaymentGatewayMessagePayload = new LitePaymentGatewayMessagePayload();
            modelMapper.map(paymentGateway, litePaymentGatewayMessagePayload);
            ProducerMessageDto producerMessageDto = ProducerMessageDto.builder()
                    .data(litePaymentGatewayMessagePayload)
                    .eventCategory(EventType.PENDING_TRANSACTION_SETTLEMENT)
                    .build();
            ikafkaMessageProducer.send("merchant.settlement", producerMessageDto);
            log.info("------||||SUCCESSFULLY PUBLISHED PENDING SETTLEMENT FOR PROCESSING {}||||--------", producerMessageDto);
        }
    }
}
