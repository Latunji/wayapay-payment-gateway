package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.core.Response;

@FeignClient(
        name = "notificationServiceEmailNotification",
        url = "${waya.notification-service.baseurl}",
        configuration = PaymentGatewayClientConfiguration.class
)
public interface NotificationServiceProxy {
    @PostMapping("/transaction/email-notification")
    Response sendEmailNotificationTransaction(@RequestBody Object request);
}
