package com.wayapaychat.paymentgateway.proxy;


import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.*;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TokenizationResponse;
import com.wayapaychat.paymentgateway.pojo.waya.wallet.TokenizePayment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;


@FeignClient(name = "ISWSERVICE", url = "${waya.isw-service.baseurl}", configuration = PaymentGatewayClientConfiguration.class)
public interface ISWService {

    @PostMapping("/cardpayment/tokenizecard")
    TokenizationResponse tokenizeCard(@RequestBody CardTokenization tokenize);

    @PostMapping("/cardpayment/tokenizepay")
    TokenizationResponse tokenPayment(@RequestBody TokenizePayment tokenize);
}
