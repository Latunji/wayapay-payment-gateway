package com.wayapaychat.paymentgateway.pojo.waya.wallet;

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
    private String deviceInformation;
}
