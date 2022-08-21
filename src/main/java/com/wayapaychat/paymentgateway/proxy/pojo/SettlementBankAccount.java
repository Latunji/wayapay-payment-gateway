package com.wayapaychat.paymentgateway.proxy.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementBankAccount {
    private String settlementBankAccountId;
    private String bankName;
    private String accountNumber;
    private String accountName;
}
