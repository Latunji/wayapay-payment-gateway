package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import com.wayapaychat.paymentgateway.pojo.waya.merchant.MerchantData;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DefaultResponse {

    private String timestamp;

    private boolean status;

    private String message;

    private String data;
}
