package com.wayapaychat.paymentgateway.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.unifiedpayment.UnifiedPaymentRequest;

@FeignClient(name = "${waya.unified-payment.name}", url = "${waya.unified-payment.baseurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface UnifiedPaymentApiClient {
	
	@PostMapping()
	public String postMerchantRequest(@RequestBody UnifiedPaymentRequest payment);

}
