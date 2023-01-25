package com.wayapaychat.paymentgateway.proxy;

import feign.Feign;
import feign.Headers;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public interface PushNotifier {
    
    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    void postObject(Object obj);

    static void postObjectToUrl(Object obj, String url) {
        PushNotifier client = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(PushNotifier.class, url);
        client.postObject(obj);
    }
    
}
