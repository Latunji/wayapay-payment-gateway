package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WayaWalletPayment {
    private String accountNo;
    private String pin;
    private String refNo;
}
