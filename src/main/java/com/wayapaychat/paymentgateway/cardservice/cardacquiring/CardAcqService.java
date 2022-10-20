package com.wayapaychat.paymentgateway.cardservice.cardacquiring;


import com.wayapaychat.paymentgateway.apihelper.API;
import com.wayapaychat.paymentgateway.cardservice.CardPaymentRequest;
import com.wayapaychat.paymentgateway.cardservice.CardTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Transactional
@Slf4j
@Service
public class CardAcqService {

    @Autowired
    private API api;

    @Value("${waya.card-acquiring-service.baseurl}")
    private String baseUrl;


    public CardTransactionResponse cardPayment (CardPaymentRequest cardPaymentRequest){
        CardTransRequest request = CardTransRequest.builder()
                .amount(cardPaymentRequest.getAmount())
                .cardNumber(cardPaymentRequest.getCardNumber())
                .currency(cardPaymentRequest.getCurrency())
                .customerId(cardPaymentRequest.getCustomerId())
                .expirationMonth(cardPaymentRequest.getExpirationMonth())
                .expirationYear(cardPaymentRequest.getExpirationYear())
                .merchantId(cardPaymentRequest.getMerchantId())
                .merchantName(cardPaymentRequest.getMerchantName())
                .mode(cardPaymentRequest.getMode())
                .securityCode(cardPaymentRequest.getSecurityCode())
                .transactionId(cardPaymentRequest.getTransactionId())
                .build();
            Map<String,String> map = new HashMap();
        CardTransactionResponse response = api.post(baseUrl+"/card/payment", request, CardTransactionResponse.class, map);
        return response;
    }


    public CardTransactionResponse pinRequest (PinRequest request){
        Map<String,String> map = new HashMap();
        CardTransactionResponse response = api.post(baseUrl+"/card/pin", request, CardTransactionResponse.class, map);
        return response;
    }

    public CardTransactionResponse authorisation (CardAuthorizationRequest request){
        Map<String,String> map = new HashMap();
        CardTransactionResponse response = api.post(baseUrl+"/card/authorisation", request, CardTransactionResponse.class, map);
        return response;
    }
}
