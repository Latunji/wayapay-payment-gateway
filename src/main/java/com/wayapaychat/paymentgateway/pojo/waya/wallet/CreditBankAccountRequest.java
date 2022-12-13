package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditBankAccountRequest {
    @NotNull
    @Size(min=3, max=50)
    private String bankName;

    @NotNull
    @Size(min=3, max=10)
    private String bankCode;

    @NotNull
    @Size(min=10, max=10)
    private String crAccount;

    @NotNull
    @Size(min=5, max=50)
    private String crAccountName;

    @NotNull
    private String amount;

    @NotNull
    @Size(min=3, max=5)
    private String transactionPin;

    @NotNull
    @Size(min=3, max=10)
    private String walletAccountNo;

    @NotNull
    private String userId;

    @NotNull
    private boolean saveBen;

    @NotNull
    @Size(min=5, max=50)
    private String narration;

    @NotNull
    @Size(min=3, max=50)
    private String transRef;


}
