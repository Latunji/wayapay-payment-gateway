package com.wayapaychat.paymentgateway.proxy;


import com.wayapaychat.paymentgateway.pojo.waya.ApiResponseBody;
import com.wayapaychat.paymentgateway.pojo.waya.LogRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "LOGGING-SERVICE-API", url = "${waya.logging-service.base-url}")
public interface LoggingProxy {
    @PostMapping("/log/create")
    ApiResponseBody<LogRequest> saveNewLog(@RequestBody LogRequest logPojo);
}
