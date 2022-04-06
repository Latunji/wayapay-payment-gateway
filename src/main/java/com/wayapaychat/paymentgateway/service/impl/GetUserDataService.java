package com.wayapaychat.paymentgateway.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.paymentgateway.pojo.waya.TokenCheckResponse;
import com.wayapaychat.paymentgateway.proxy.AuthApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;


@Component
@Slf4j
public class GetUserDataService {

    @Autowired
    private AuthApiClient authProxy;
    @Autowired
    private ObjectMapper objectMapper;

    public TokenCheckResponse getUserData(String token) {
        LinkedHashMap<String, Object> linkedHashMap = authProxy.getUserDataToken(token);
        log.info("{}", linkedHashMap);
        return objectMapper.convertValue(linkedHashMap, TokenCheckResponse.class);
    }
}
