package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.common.enums.RecurrentPaymentStatus;
import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.CustomerRequest;
import com.wayapaychat.paymentgateway.pojo.waya.PaymentLinkResponsePojo;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantCustomer;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@FeignClient(name = "${waya.identitymanager.name}", url = "${waya.identitymanager.url}", configuration = PaymentGatewayClientConfiguration.class)
public interface IdentityManagementServiceProxy {

    @GetMapping("/waya-merchant/{merchantId}")
    MerchantResponse getMerchantDetail(@RequestHeader("Authorization") String authorization, @PathVariable("merchantId") String merchantId);

    @PostMapping("/webpos/customer/no-auth")
    MerchantCustomer postCustomerCreate(@RequestBody CustomerRequest request, @RequestHeader("authorization") String token);

    @GetMapping("/waya-merchant/auth-user/merchant-account")
    MerchantResponse getMerchantAccount(@RequestHeader("Authorization") String authorization);

    @GetMapping("/webpos/payment-link/no-auth/fetch/{paymentLinkId}")
    PaymentLinkResponsePojo getPaymentLinkDetailsById(@RequestHeader("Authorization") String authorization, @PathVariable("paymentLinkId") String paymentLinkId);

    @GetMapping("/waya-merchant/configuration/fetch")
    WayaMerchantConfigurationResponse getMerchantConfiguration(@RequestParam(required = false) String merchantId, @NotNull @NotEmpty @RequestHeader("Authorization") String authorizationToken);

    @PutMapping("/webpos/customer-subscription/status-update/{paymentLinkId}")
    WayaMerchantConfigurationResponse updateCustomerSubscriptionStatus(
            @PathVariable String paymentLinkId,
            @RequestParam RecurrentPaymentStatus status,
            @RequestParam(required = false) String subscriptionStatusUpdateReason,
            @NotNull @NotEmpty @RequestHeader("Authorization") String authorizationToken);

}
