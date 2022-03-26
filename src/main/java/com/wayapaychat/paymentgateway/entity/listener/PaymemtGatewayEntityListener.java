package com.wayapaychat.paymentgateway.entity.listener;


import com.wayapaychat.paymentgateway.entity.PaymentGateway;
import com.wayapaychat.paymentgateway.enumm.EventCategory;
import com.wayapaychat.paymentgateway.enumm.EventType;
import com.wayapaychat.paymentgateway.enumm.ProductType;
import com.wayapaychat.paymentgateway.enumm.TransactionStatus;
import com.wayapaychat.paymentgateway.pojo.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.NotificationPojo;
import com.wayapaychat.paymentgateway.pojo.PaymentData;
import com.wayapaychat.paymentgateway.pojo.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.notification.EmailStreamData;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationReceiver;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationServiceResponse;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationStreamData;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.NotificationServiceProxy;
import com.wayapaychat.paymentgateway.utils.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PaymemtGatewayEntityListener {
    private static final String CURRENCY_DISPLAY = "NGN";
    private static NotificationServiceProxy notificationServiceProxy;
    private static AuthApiClient authApiClient;
    private static VariableUtil variableUtil;

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

    @PostPersist
    @PostUpdate
    public void sendTransactionNotificationAfterPaymentIsSuccessful(PaymentGateway paymentGateway) throws Exception {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        log.info("------||||PREPROCESSING TRANSACTION BEFORE SENDING NOTIFICATION WITH TRANSACTION ID: {}||||--------",
                paymentGateway.getTranId());
        if (paymentGateway.getStatus() == TransactionStatus.SUCCESSFUL
                || paymentGateway.getStatus() == TransactionStatus.TRANSACTION_COMPLETED) {
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
                    .currency(getCurrencyName(paymentGateway.getCurrencyCode()))
                    .updatedAt(paymentGateway.getVendorDate() == null ? paymentGateway.getTranDate() : paymentGateway.getVendorDate())
                    .build();
            processEmailAlert(notificationPojo);
        }
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
        emailStreamData.setNarration(String.format("Transaction was successfully processed with %s Channel",notificationPojo.getPaymentChannel()));
        emailStreamData.setAmount(notificationPojo.getTransactionAmount().toString());
        emailStreamData.setTransactionDate(notificationPojo.getUpdatedAt().toString());
        emailStreamData.setMode(variableUtil.getMode());

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

    private String getDaemonAuthToken() throws Exception {
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

    private String getCurrencyName(String currencyCode) {
        Optional<Currency> currency = Currency.getAvailableCurrencies().stream().filter(c -> (c.getNumericCode() + "").equals(currencyCode)).findAny();
        if (currency.isPresent())
            return currency.get().getCurrencyCode();
        return CURRENCY_DISPLAY;
    }
}
