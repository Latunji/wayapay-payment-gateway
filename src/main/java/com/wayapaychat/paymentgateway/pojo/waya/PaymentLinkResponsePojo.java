package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponsePojo {
    private String code;
    private String date;
    private String message;
    private PaymentLinkResponse data;
}
