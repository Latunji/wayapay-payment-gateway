package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WithdrawalResponse {

    private String timeStamp;

    private boolean status;

    private String message;

    private String data;
}
