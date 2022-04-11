package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletQRGenerate;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletQRResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${waya.qrcode.name}", url = "${waya.qrcode.baseurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface QRCodeProxy {

    @PostMapping("/qrcode/generate-payment-qrcode")
	WalletQRResponse wayaQRGenerate(@RequestHeader("Authorization") String authorization, @RequestBody WalletQRGenerate request);

}
