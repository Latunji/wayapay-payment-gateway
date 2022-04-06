package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.WalletEventPayment;
import com.wayapaychat.paymentgateway.pojo.waya.WalletOfficePayment;
import com.wayapaychat.paymentgateway.pojo.waya.WalletPaymentResponse;
import com.wayapaychat.paymentgateway.pojo.waya.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${waya.wallet.tempname}", url = "${waya.wallet.tempurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface WalletProxy {

    @GetMapping("/wallet/accounts/{user_id}")
	WalletResponse getWalletDetails(@RequestHeader("authorization") String token, @PathVariable("user_id") long user_id);

    @PostMapping("/wallet/event/charge/payment")
	WalletPaymentResponse fundWayaAccount(@RequestHeader("authorization") String token, @RequestBody WalletEventPayment request);

    @PostMapping("/wallet/event/office/payment")
	WalletPaymentResponse fundOfficialAccount(@RequestHeader("authorization") String token, @RequestBody WalletOfficePayment request);


}
