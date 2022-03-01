package com.wayapaychat.paymentgateway.proxy;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.LoginRequest;
import com.wayapaychat.paymentgateway.pojo.ProfileResponse;
import com.wayapaychat.paymentgateway.pojo.TokenAuthResponse;
import com.wayapaychat.paymentgateway.pojo.TokenCheckResponse;



@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface AuthApiClient {

    
    @PostMapping("/auth/validate-user")
	public TokenCheckResponse getUserDataToken(@RequestHeader("authorization") String token);
    
    @PostMapping("/auth/login")
	public TokenAuthResponse UserLogin(@RequestBody LoginRequest login);
    
    @GetMapping("/profile/{id}")
    ProfileResponse getProfileDetail(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
    
    
}
