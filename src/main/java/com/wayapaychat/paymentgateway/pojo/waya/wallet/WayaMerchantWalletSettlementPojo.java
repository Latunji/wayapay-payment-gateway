package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WayaMerchantWalletSettlementPojo {
    @NotBlank(message = "Office Account must not Null or Blank")
    @Size(min = 12, max = 16, message = "Account must be 15 digit")
    private String officeDebitAccount;

    // @NotNull(message = "Account must be 10 digit")
    @NotBlank(message = "Customer Account must not Null or Blank")
    @Size(min = 10, max = 10, message = "Account must be 10 digit")
    private String customerCreditAccount;

    @NotNull
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    // @NotNull
    @NotBlank(message = "tranType must not Null or Blank")
    private String tranType;

    // @NotNull
    @NotBlank(message = "tranCrncy must not Null or Blank")
    @Size(min = 3, max = 5, message = "tranCrncy must be 3 alphanumeric (NGN)")
    private String tranCrncy;

    // @NotNull
    @NotBlank(message = "tranNarration must not Null or Blank")
    @Size(min = 5, max = 255, message = "tranNarration must be between 5 and 255")
    private String tranNarration;

    // @NotNull
    @NotBlank(message = "paymentReference must not Null or Blank")
    @Size(min = 3, max = 255, message = "paymentReference must be betweeen 3 and 255")
    private String paymentReference;

}
