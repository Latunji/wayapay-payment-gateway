package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.DefaultResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WithdrawalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${waya.withdrawal.name}", url = "${waya.withdrawal.url}", configuration = PaymentGatewayClientConfiguration.class)


public interface WithdrawalProxy {

    @PostMapping("/withdrawal/fund")
    DefaultResponse withdrawFromWallet(@RequestHeader("authorization") String token, @RequestBody WithdrawalRequest withdrawalRequest);
}