package com.wayapaychat.paymentgateway.proxy;


import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;


@FeignClient(name = "${waya.wallet.auth}", url = "${waya.wallet.authurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface AuthApiClient {


    @PostMapping("/auth/validate-user")
    LinkedHashMap<String,Object> getUserDataToken(@RequestHeader("authorization") String token);

    @PostMapping("/auth/login")
    TokenAuthResponse authenticateUser(@RequestBody LoginRequest login);

    @GetMapping("/profile/{id}")
    ProfileResponse getProfileDetail(@PathVariable("id") Long id, @RequestHeader("authorization") String token);

    @GetMapping("/pin/validate-pin/{userId}/{pin}")
    PinResponse validatePin(@PathVariable("userId") Long userId, @PathVariable("pin") Long pin, @RequestHeader("authorization") String token);


    @GetMapping("/user/email/{email}")
    ApiResponseBody<AuthenticatedUser> getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/user/phone/{phoneNumber}")
    ApiResponseBody<AuthenticatedUser> getUserByPhoneNumber(@PathVariable("phoneNumber") String phoneNumber);

    @GetMapping("/user/{id}")
    ApiResponseBody<AuthenticatedUser> getUserById(@PathVariable("id") Long id);
}
