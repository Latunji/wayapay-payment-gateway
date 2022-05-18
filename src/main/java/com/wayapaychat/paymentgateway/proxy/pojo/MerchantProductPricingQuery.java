package com.wayapaychat.paymentgateway.proxy.pojo;

import com.wayapaychat.paymentgateway.common.enums.ProductName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProductPricingQuery {
    @NotNull(message = "merchantId can not be null")
    private String merchantId;
    @NotNull(message = "productName can not be null")
    private ProductName productName;
}
