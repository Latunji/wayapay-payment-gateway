package com.wayapaychat.paymentgateway.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static String getBearerTokenHeader() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest().getHeader("Authorization");
        }
        return null;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = getBearerTokenHeader();
        if (token != null && !token.isBlank()) {
            requestTemplate.header(AUTHORIZATION_HEADER, token);
        }
    }
}