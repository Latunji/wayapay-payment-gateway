package com.wayapaychat.paymentgateway.config;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Slf4j
public class PaymentGatewayClientConfiguration implements ErrorDecoder {

    public Encoder feignEncoder() {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new SpringEncoder(objectFactory);
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = feign.FeignException.errorStatus(methodKey, response);
        int status = response.status();
        // this code is irrelevant here; TODO: do this in a separate function and call it when needed
//        if (status >= 500) {
//            log.error("-------||| RETRYING REQUEST ------|||| {0}", exception);
//            return new RetryableException(
//                    response.status(),
//                    exception.getMessage(),
//                    response.request().httpMethod(),
//                    exception, null,
//                    response.request());
//        }
        return exception;
    }
}
