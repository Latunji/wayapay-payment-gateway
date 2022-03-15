package com.wayapaychat.paymentgateway.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.WalletQRGenerate;
import com.wayapaychat.paymentgateway.pojo.waya.WalletQRResponse;

@FeignClient(name = "${waya.qrcode.name}", url = "${waya.qrcode.baseurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface QRCodeProxy {
	
	@PostMapping("/qrcode/generate-payment-qrcode")
	public WalletQRResponse wayaQRGenerate(@RequestBody WalletQRGenerate request);

}
