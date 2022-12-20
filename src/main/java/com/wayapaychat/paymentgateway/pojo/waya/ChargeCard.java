package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ChargeCard {

    @NotBlank(message = "CustomerId is required")
    @NotNull(message = "CustomerId cannot be null")
    @NotEmpty(message = "CustomerId cannot be empty")
    private String customerId;
    @NotBlank(message = "Wallet account number is required")
    @NotNull(message = "Wallet account number cannot be null")
    @NotEmpty(message = "Wallet account number be empty")
    private String walletAccountNo;
    @NotBlank(message = "Card Number is required")
    @NotNull(message = "Card Number cannot be null")
    @NotEmpty(message = "Card Number cannot be empty")
    private String cardNumber;
    @NotBlank(message = "Amount is required")
    @NotNull(message = "Amount cannot be null")
    @NotEmpty(message = "Amount cannot be empty")
    private String amount;
    @NotBlank(message = "Transaction Ref is required")
    @NotNull(message = "Transaction Ref cannot be null")
    @NotEmpty(message = "Transaction Ref cannot be empty")
    private String transRef;
}
