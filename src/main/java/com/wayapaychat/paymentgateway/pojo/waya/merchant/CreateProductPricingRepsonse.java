package com.wayapaychat.paymentgateway.pojo.waya.merchant;

import lombok.Data;

import java.util.List;

@Data
public class CreateProductPricingRepsonse {
    private String code;
    private String date;
    private String message;
    private List<ProductPricingResponse> data;
}
