package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${waya.wallet.tempname}", url = "${waya.wallet.tempurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface WalletProxy {

    @GetMapping("/wallet/accounts/{user_id}")
    WalletResponse getWalletDetails(@RequestHeader("authorization") String token, @PathVariable("user_id") long user_id);

    @GetMapping("/wallet/default/{userId}")
    DefaultWalletResponse getUserDefaultWalletAccount(@RequestHeader("authorization") String token, @PathVariable("userId") long userId);

    @PostMapping("/wallet/event/charge/payment")
    WalletPaymentResponse fundWayaAccount(@RequestHeader("authorization") String token, @RequestBody WalletEventPayment request);

    @PostMapping("/wallet/event/office/payment")
    WalletPaymentResponse fundOfficialAccount(@RequestHeader("authorization") String token, @RequestBody WalletOfficePayment request);

    @PostMapping("/wallet/official/user/transfer")
    WalletSettlementResponse creditMerchantDefaultWallet(@RequestHeader("authorization") String token, WayaMerchantWalletSettlementPojo wayaMerchantWalletSettlementPojo);

    @PostMapping("/wallet/event/charge/payment")
    WalletSettlementResponse creditMerchantDefaultWalletWithEventId(@RequestHeader("authorization") String token, WalletSettlementWithEventIdPojo walletSettlementWithEventIdPojo);

    @PostMapping("/wallet/fund/bank/account")
    WalletSettlementResponse creditBankAccount(@RequestHeader("authorization") String token, CreditBankAccountRequest creditBankAccountRequest);

}
