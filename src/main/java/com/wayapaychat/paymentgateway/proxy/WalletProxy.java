package com.wayapaychat.paymentgateway.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.WalletEventPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WalletOfficePayment;
import com.wayapaychat.paymentgateway.pojo.waya.WalletPaymentResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WalletResponse;

@FeignClient(name = "${waya.wallet.tempname}", url = "${waya.wallet.tempurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface WalletProxy {
	
	@GetMapping("/wallet/accounts/{user_id}")
	public WalletResponse getWalletDetails(@RequestHeader("authorization") String token, @PathVariable("user_id") long user_id);
	
	@PostMapping("/wallet/event/charge/payment")
	public WalletPaymentResponse fundWayaAccount(@RequestHeader("authorization") String token, @RequestBody WalletEventPayment request);
	
	@PostMapping("/wallet/event/office/payment")
	public WalletPaymentResponse fundOfficialAccount(@RequestHeader("authorization") String token, @RequestBody WalletOfficePayment request);


}
