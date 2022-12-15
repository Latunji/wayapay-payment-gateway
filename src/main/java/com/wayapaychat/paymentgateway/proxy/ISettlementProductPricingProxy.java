package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.common.enums.ProductName;
import com.wayapaychat.paymentgateway.config.PaymentGatewayClientConfiguration;
import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantProductPricingResponse;
import com.wayapaychat.paymentgateway.proxy.pojo.MerchantProductPricingQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@FeignClient(name = "${waya.settlementservice.name}", url = "${waya.settlementservice.url}", configuration = PaymentGatewayClientConfiguration.class)
public interface ISettlementProductPricingProxy {

    @GetMapping("/merchant-product-pricing/fetch-product-pricing")
    MerchantProductPricingResponse getMerchantProductPricing(
            @RequestParam String merchantId,
            @RequestParam ProductName productName,
            @NotNull @NotEmpty @RequestHeader("Authorization") String token);
}
