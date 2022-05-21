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
public class WalletSettlementWithEventIdPojo {

    @NotNull
    @Size(min = 6, max = 50)
    private String eventId;

    @NotNull
    @Size(min = 10, max = 10)
    private String customerAccountNumber;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @Size(min = 3, max = 5)
    private String tranCrncy;

    @NotNull
    @Size(min = 5, max = 50)
    private String tranNarration;

    @NotNull
    @Size(min = 3, max = 50)
    private String paymentReference;

    @NotNull
    @Size(min = 3, max = 20)
    private String transactionCategory;
    private String merchantId;

}
