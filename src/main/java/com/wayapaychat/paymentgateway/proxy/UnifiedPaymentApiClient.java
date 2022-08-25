package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "${waya.unified-payment.name}", url = "${waya.unified-payment.baseurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface UnifiedPaymentApiClient {

    @PostMapping()
	String postMerchantRequest(@RequestBody UnifiedPaymentRequest payment);

}
