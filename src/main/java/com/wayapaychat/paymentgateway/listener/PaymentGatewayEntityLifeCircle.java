package com.wayapaychat.paymentgateway.listener;


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
import com.wayapaychat.paymentgateway.pojo.notification.NotificationStreamData;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import com.wayapaychat.paymentgateway.proxy.NotificationServiceProxy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import java.util.Currency;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentGatewayEntityLifeCircle {
    private final NotificationServiceProxy notificationServiceProxy;
    @Value("${application.payment-gateway-mode}")
    private String mode;
    private AuthApiClient authApiClient;
    @Value("${service.name}")
    private String userName;
    @Value("${service.pass}")
    private String password;

    @PostPersist
    @PostUpdate
    private void checkPaymentGatewayUpdate(PaymentGateway paymentGateway) throws Exception {
        log.info("------||||PREPROCESSING TRANSACTION BEFORE SENDING NOTIFICATION WITH TRANSACTION ID: {}||||--------",
                paymentGateway.getTranId());
        if (paymentGateway.getStatus() == TransactionStatus.SUCCESSFUL) {
            NotificationPojo notificationPojo = NotificationPojo
                    .builder()
                    .paymentChannel(paymentGateway.getChannel())
                    .merchantName(paymentGateway.getMerchantName())
                    .transactionDate(paymentGateway.getVendorDate())
                    .transactionAmount(paymentGateway.getAmount())
                    .transactionMode(mode)
                    .paymentGatewayTransactionId(paymentGateway.getTranId())
                    .currency(Currency.getInstance(paymentGateway.getCurrencyCode()).getDisplayName())
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
        emailStreamData.setNarration(notificationPojo.getTransactionNarration());
        emailStreamData.setNarration("Transaction was successful with USSD Channel");
        emailStreamData.setAmount(notificationPojo.getTransactionAmount().toString());
        emailStreamData.setTransactionDate(notificationPojo.getUpdatedAt().toString());
        emailStreamData.setMode(mode);

        notificationStreamData.setNames(List.of(NotificationReceiver.builder()
                .email(notificationPojo.getCustomerEmailAddress())
                .fullName(notificationPojo.getCustomerName())
                .build()));
        emailStreamData.setData(notificationStreamData);
        emailStreamData.setForMerchant(false + "");
        notificationServiceProxy.sendEmailNotificationTransaction(notificationPojo, getDaemonAuthToken());
        log.info("------||||NOTIFICATION HAS BEEN SENT TO CUSTOMER EMAIL ADDRESS {}||||--------", notificationPojo.getCustomerEmailAddress());

        notificationStreamData.setNames(List.of(NotificationReceiver.builder()
                .email(notificationPojo.getMerchantEmailAddress())
                .fullName(notificationPojo.getMerchantName())
                .build()));
        emailStreamData.setData(notificationStreamData);
        emailStreamData.setForMerchant(true + "");
        notificationServiceProxy.sendEmailNotificationTransaction(notificationPojo, getDaemonAuthToken());
        log.info("------||||NOTIFICATION HAS BEEN SENT TO MERCHANT EMAIL ADDRESS {}||||--------", notificationPojo.getMerchantEmailAddress());
    }

    private String getDaemonAuthToken() throws Exception {
        TokenAuthResponse authToken = authApiClient.authenticateUser(
                LoginRequest.builder()
                        .password(password)
                        .emailOrPhoneNumber(userName)
                        .build());
        log.info("AUTHENTICATION RESPONSE: " + authToken.toString());
        if (!authToken.getStatus()) {
            log.info("------||||FAILED TO AUTHENTICATE DAEMON USER [email: {} , password: {}]||||--------", userName, password);
            throw new Exception("Failed to process user authentication...!");
        }
        PaymentData payData = authToken.getData();
        return payData.getToken();
    }
}
