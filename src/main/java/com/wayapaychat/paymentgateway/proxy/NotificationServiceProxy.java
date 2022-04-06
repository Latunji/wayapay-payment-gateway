package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.notification.EmailStreamData;
import com.wayapaychat.paymentgateway.pojo.notification.NotificationServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@FeignClient(
        name = "notificationServiceEmailNotification",
        url = "${waya.notification-service.baseurl}",
        configuration = PaymentGatewayClientConfiguration.class
)
public interface NotificationServiceProxy {
    @PostMapping("/email-notification-wayapay")
    NotificationServiceResponse sendEmailNotificationTransaction(
            @RequestBody EmailStreamData request,
            @NotNull @NotEmpty @RequestHeader(name = "Authorization") String token);
}
