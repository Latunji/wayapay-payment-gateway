package com.wayapaychat.paymentgateway.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.WalletResponse;

@FeignClient(name = "${waya.wallet.tempname}", url = "${waya.wallet.tempurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface WalletProxy {
	
	@GetMapping("/wallet/accounts/{user_id}")
	public WalletResponse getWalletDetails(@RequestHeader("authorization") String token, @PathVariable("user_id") long user_id);

}
