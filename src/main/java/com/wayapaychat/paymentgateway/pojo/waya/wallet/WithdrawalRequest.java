package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WithdrawalRequest {

    private String amount;
    private String bankCode;
    private String bankName;
    private String crAccount;
    private String crAccountName;
    private String narration;
    private boolean saveBen;
    private String transRef;
    private String transactionPin;
    private String userId;
    private String walletAccountNo;

}
