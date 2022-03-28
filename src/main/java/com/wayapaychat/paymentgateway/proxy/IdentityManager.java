package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.CustomerRequest;
import com.wayapaychat.paymentgateway.pojo.MerchantCustomer;
import com.wayapaychat.paymentgateway.pojo.MerchantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${waya.identitymanager.name}", url = "${waya.identitymanager.url}", configuration = PaymentGatewayClientConfiguration.class)
public interface IdentityManager {

    @GetMapping("/waya-merchant/{merchantId}")
	MerchantResponse getMerchantDetail(@RequestHeader("authorization") String token, @PathVariable("merchantId") String merchantId);

    @PostMapping("/webpos/customer/no-auth")
	MerchantCustomer postCustomerCreate(@RequestBody CustomerRequest request, @RequestHeader("authorization") String token);

    @GetMapping("/waya-merchant/auth-user/merchant-account")
	MerchantResponse getMerchantAccount();
}
