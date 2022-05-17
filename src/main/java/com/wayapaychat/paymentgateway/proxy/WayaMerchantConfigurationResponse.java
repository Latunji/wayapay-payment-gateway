package com.wayapaychat.paymentgateway.proxy;

import com.wayapaychat.paymentgateway.pojo.waya.PaymentLinkResponse;
import com.wayapaychat.paymentgateway.proxy.pojo.WayaMerchantConfiguration;
import lombok.Data;

@Data
public class WayaMerchantConfigurationResponse {
    private String code;
    private String date;
    private String message;
    private Pro data;
}
