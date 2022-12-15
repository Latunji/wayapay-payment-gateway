package com.wayapaychat.paymentgateway.pojo.waya.merchant;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MerchantProductPricingResponse {
    private String code;
    private String date;
    private String message;
    private ProductPricingResponse data;
}
