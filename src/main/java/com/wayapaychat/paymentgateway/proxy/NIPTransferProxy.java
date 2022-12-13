package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.NIPTransferRequest;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.DefaultWalletResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${waya.wallet.tempname}", url = "${waya.wallet.tempurl}", configuration = PaymentGatewayClientConfiguration.class)

public interface NIPTransferProxy {

    @PostMapping("/api/v1/wayama/NIPBank/fundTransfer")
    DefaultWalletResponse creditBankAccount(@RequestHeader("authorization") String token, @RequestHeader NIPTransferRequest nipTransferRequest);
}
