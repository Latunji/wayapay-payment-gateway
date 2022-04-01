package com.wayapaychat.paymentgateway.common.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class VariableUtil {
    @Value("${waya.application.payment-gateway-mode}")
    private String mode;
    @Value("${service.name}")
    private String userName;
    @Value("${service.pass}")
    private String password;
}
