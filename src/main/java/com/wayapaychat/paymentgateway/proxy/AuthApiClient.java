package com.wayapaychat.paymentgateway.proxy;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.TokenCheckResponse;



@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface AuthApiClient {

    
    @PostMapping("/auth/validate-user")
	public TokenCheckResponse getUserDataToken(@RequestHeader("authorization") String token);
    
    
}
