package com.wayapaychat.paymentgateway.pojo.unifiedpayment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedPaymentRecurrentPaymentRequest {
    @Value("${waya.unified-payment.secret}")
    private String SecretKey;
    private String Scheme;
    private Float Amount;
    private Float Fee;
    private String SessionId;
    private String Currency = "566";
    private String CustomerEmail;
    private String CustomerName;
    private String Description;
    @Value(value = "${waya.callback}")
    private String ReturnUrl;
}
