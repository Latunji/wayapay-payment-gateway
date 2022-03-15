package com.wayapaychat.paymentgateway.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.CustomerRequest;
import com.wayapaychat.paymentgateway.pojo.MerchantCustomer;
import com.wayapaychat.paymentgateway.pojo.MerchantResponse;

@FeignClient(name = "${waya.identitymanager.name}", url = "${waya.identitymanager.url}", configuration = PaymentGatewayClientConfiguration.class)
public interface IdentityManager {
	
	@GetMapping("/waya-merchant/{merchantId}")
	public MerchantResponse getMerchantDetail(@RequestHeader("authorization") String token, @PathVariable("merchantId") String merchantId);
	
	@PostMapping("/webpos/customer/no-auth")
	public MerchantCustomer postCustomerCreate(@RequestBody CustomerRequest request, @RequestHeader("authorization") String token);

}
